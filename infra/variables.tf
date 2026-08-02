variable "project_name" {
  description = "Project name used as a prefix for all resources"
  type        = string
}

variable "aws_region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-east-2"
}

variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "ssh_public_key" {
  description = "Public SSH key material for EC2 key pair"
  type        = string
  sensitive   = true
}
