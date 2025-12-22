variable "subscription_id" {
  description = "Azure Subscription ID"
  type        = string
  sensitive   = true
}

variable "tenant_id" {
  description = "Azure Tenant ID"
  type        = string
  sensitive   = true
}

variable "client_id" {
  description = "Azure Client ID (Service Principal)"
  type        = string
  sensitive   = true
}

variable "client_secret" {
  description = "Azure Client Secret (Service Principal)"
  type        = string
  sensitive   = true
}

variable "location" {
  description = "Azure region for resources"
  type        = string
  default     = "eastus"
}

variable "project_name" {
  description = "Project name for resource naming"
  type        = string
  default     = "spring-boot-accelerator"
}

variable "sql_admin_username" {
  description = "SQL Server administrator username"
  type        = string
  sensitive   = true
  default     = "sqladmin"
}

variable "sql_admin_password" {
  description = "SQL Server administrator password"
  type        = string
  sensitive   = true
}

variable "azuread_admin_login" {
  description = "Azure AD admin login (email or display name)"
  type        = string
}

variable "azuread_admin_object_id" {
  description = "Azure AD admin object ID"
  type        = string
}

variable "local_ip_address" {
  description = "Your local IP address for SQL firewall access"
  type        = string
}

variable "container_image" {
  description = "Container image URI (e.g., ghcr.io/danielpaval/spring-boot-accelerator:latest)"
  type        = string
  default     = "ghcr.io/danielpaval/spring-boot-accelerator:latest"
}

variable "container_port" {
  description = "Port the Spring Boot app listens on"
  type        = number
  default     = 8080
}

variable "container_cpu" {
  description = "CPU allocation for container (e.g., 0.5, 1, 2)"
  type        = string
  default     = "0.5"
}

variable "container_memory" {
  description = "Memory allocation for container (e.g., 1Gi, 2Gi)"
  type        = string
  default     = "1Gi"
}

variable "container_min_replicas" {
  description = "Minimum number of container replicas"
  type        = number
  default     = 1
}

variable "container_max_replicas" {
  description = "Maximum number of container replicas"
  type        = number
  default     = 3
}

variable "jwt_issuer_uri" {
  description = "JWT issuer URI for authentication"
  type        = string
}

variable "app_redirect_uris" {
  description = "Redirect URIs for the OAuth application"
  type        = list(string)
  default     = ["http://localhost:8080/callback"]
}
