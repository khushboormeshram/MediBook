variable "aws_region" {
  description = "AWS region where resources will be provisioned"
  type        = string
  default     = "ap-south-1"
}

variable "project_name" {
  description = "Name of the project used for naming and resource tagging"
  type        = string
  default     = "medibook"
}

variable "environment" {
  description = "Deployment environment (e.g., development, staging, production)"
  type        = string
  default     = "production"
}

variable "instance_type" {
  description = "EC2 instance type for running MediBook (t2.micro / t3.micro are AWS Free Tier eligible)"
  type        = string
  default     = "t2.micro"
}

variable "trusted_ssh_cidr" {
  description = "List of trusted CIDR blocks allowed to connect via SSH (Port 22). Set this to your public IP /32 for deployments that need SSH access; the default creates no SSH ingress rule."
  type        = list(string)
  default     = []

  validation {
    condition     = alltrue([for cidr in var.trusted_ssh_cidr : can(cidrhost(cidr, 0))])
    error_message = "Every trusted_ssh_cidr entry must be a valid CIDR block, for example 203.0.113.10/32."
  }
}

variable "enable_ssh_key_generation" {
  description = "Whether to automatically generate a dedicated TLS SSH private key pair. If false, existing_key_pair_name must be supplied."
  type        = bool
  default     = true
}

variable "existing_key_pair_name" {
  description = "Optional name of an existing AWS key pair to attach to the EC2 instance (used if enable_ssh_key_generation is false)"
  type        = string
  default     = ""
}

variable "root_volume_size" {
  description = "Root EBS storage volume size in GiB (AWS Free Tier covers up to 30 GiB)"
  type        = number
  default     = 20
}

variable "root_volume_type" {
  description = "Root EBS volume storage type"
  type        = string
  default     = "gp3"
}
