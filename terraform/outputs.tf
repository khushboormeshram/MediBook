output "instance_id" {
  description = "EC2 Instance ID"
  value       = aws_instance.medibook_server.id
}

output "public_ip" {
  description = "Public IPv4 address of the MediBook server"
  value       = aws_instance.medibook_server.public_ip
}

output "public_dns" {
  description = "Public DNS name of the MediBook server"
  value       = aws_instance.medibook_server.public_dns
}

output "application_url" {
  description = "Direct HTTP access URL for the MediBook web application"
  value       = "http://${aws_instance.medibook_server.public_ip}"
}

output "actuator_health_url" {
  description = "Spring Boot Actuator health check URL (proxied via Nginx)"
  value       = "http://${aws_instance.medibook_server.public_ip}/actuator/health"
}

output "ssh_connection_command" {
  description = "Command to connect to the EC2 instance via SSH"
  value       = var.enable_ssh_key_generation ? "ssh -i medibook-key.pem ubuntu@${aws_instance.medibook_server.public_ip}" : "ssh -i <your-key.pem> ubuntu@${aws_instance.medibook_server.public_ip}"
}

output "private_key_pem" {
  description = "Generated private key material (sensitive)"
  value       = var.enable_ssh_key_generation ? tls_private_key.ssh[0].private_key_pem : null
  sensitive   = true
}
