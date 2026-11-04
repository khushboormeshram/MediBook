# Default VPC and Subnet Discovery
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Latest Ubuntu 24.04 LTS Noble AMI
data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# SSH Key Pair Management
resource "tls_private_key" "ssh" {
  count     = var.enable_ssh_key_generation ? 1 : 0
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "generated" {
  count      = var.enable_ssh_key_generation ? 1 : 0
  key_name   = "${var.project_name}-${var.environment}-key"
  public_key = tls_private_key.ssh[0].public_key_openssh
}

resource "local_sensitive_file" "private_key" {
  count           = var.enable_ssh_key_generation ? 1 : 0
  content         = tls_private_key.ssh[0].private_key_pem
  filename        = "${path.module}/medibook-key.pem"
  file_permission = "0400"
}

locals {
  key_name = var.enable_ssh_key_generation ? aws_key_pair.generated[0].key_name : var.existing_key_pair_name
}

# Security Group for MediBook
resource "aws_security_group" "medibook_sg" {
  name_prefix = "${var.project_name}-${var.environment}-sg-"
  description = "Security Group for MediBook web application"
  vpc_id      = data.aws_vpc.default.id

  # Ingress: HTTP (Frontend & Proxied API)
  ingress {
    description = "HTTP Ingress for MediBook Web App"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # Ingress: SSH is only created when the operator supplies an explicit CIDR.
  # Keeping this dynamic avoids an accidental 0.0.0.0/0 SSH exposure.
  dynamic "ingress" {
    for_each = length(var.trusted_ssh_cidr) > 0 ? [1] : []
    content {
      description = "SSH administrative access from explicitly trusted CIDR blocks"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = var.trusted_ssh_cidr
    }
  }

  # Egress: Allow all outbound traffic (for OS updates, Docker registry pulls)
  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-sg"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# EC2 Application Host Instance
resource "aws_instance" "medibook_server" {
  ami                         = data.aws_ami.ubuntu.id
  instance_type               = var.instance_type
  key_name                    = local.key_name
  vpc_security_group_ids      = [aws_security_group.medibook_sg.id]
  subnet_id                   = data.aws_subnets.default.ids[0]
  associate_public_ip_address = true

  # Require IMDSv2 and limit the response hop for the instance metadata service.
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  root_block_device {
    volume_size           = var.root_volume_size
    volume_type           = var.root_volume_type
    delete_on_termination = true
    encrypted             = true

    tags = {
      Name = "${var.project_name}-${var.environment}-root-volume"
    }
  }

  user_data = <<-EOF
              #!/bin/bash
              set -e
              # System updates and Docker installation
              apt-get update -y
              apt-get install -y ca-certificates curl gnupg lsb-release git
              
              # Install Docker
              install -m 0755 -d /etc/apt/keyrings
              curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
              chmod a+r /etc/apt/keyrings/docker.gpg
              
              echo "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
              
              apt-get update -y
              apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
              
              # Enable service and grant permissions to ubuntu user
              systemctl enable docker
              systemctl start docker
              usermod -aG docker ubuntu
              
              # Prepare deployment workspace
              mkdir -p /opt/medibook
              chown -R ubuntu:ubuntu /opt/medibook
              
              echo "MediBook EC2 initialization complete" > /opt/medibook/init.log
              EOF

  tags = {
    Name = "${var.project_name}-${var.environment}-server"
  }
}
