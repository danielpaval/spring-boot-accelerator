data "azuread_client_config" "current" {}

resource "azuread_application" "main" {
  display_name = "${var.project_name}-app"
  owners       = [data.azuread_client_config.current.object_id]

  web {
    redirect_uris = var.app_redirect_uris

    implicit_grant {
      access_token_issuance_enabled = true
      id_token_issuance_enabled     = true
    }
  }

  # Define custom app roles that will appear in the 'roles' claim
  app_role {
    allowed_member_types = ["User"]
    description          = "Administrator role with full access"
    display_name         = "Administrator"
    enabled              = true
    id                   = "00000000-0000-0000-0000-000000000001"
    value                = "ADMIN"
  }

  app_role {
    allowed_member_types = ["User"]
    description          = "Regular user role with standard access"
    display_name         = "User"
    enabled              = true
    id                   = "00000000-0000-0000-0000-000000000002"
    value                = "USER"
  }

  required_resource_access {
    resource_app_id = "00000003-0000-0000-c000-000000000000" # Microsoft Graph

    resource_access {
      id   = "e1fe6dd8-ba31-4d61-89e7-88639da4683d" # User.Read
      type = "Scope"
    }

    resource_access {
      id   = "37f7f235-527c-4136-accd-4a02d197296e" # OpenID
      type = "Scope"
    }

    resource_access {
      id   = "7427e0e9-2fba-42fe-b0c0-848c9e6a8182" # offline_access
      type = "Scope"
    }
  }
}

resource "azuread_service_principal" "main" {
  client_id = azuread_application.main.client_id
  owners    = [data.azuread_client_config.current.object_id]
}

# Grant admin consent for delegated permissions
# This allows the app to request openid, profile, and offline_access without per-user consent
resource "azuread_service_principal_delegated_permission_grant" "graph_consent" {
  service_principal_object_id          = azuread_service_principal.main.object_id
  resource_service_principal_object_id = data.azuread_service_principal.graph.object_id
  claim_values                         = ["openid", "profile", "offline_access"]
}

# Reference to Microsoft Graph service principal
data "azuread_service_principal" "graph" {
  client_id = "00000003-0000-0000-c000-000000000000" # Microsoft Graph
}

resource "azuread_application_password" "main" {
  application_id    = azuread_application.main.id
  display_name      = "terraform-generated-secret"
  end_date_relative = "8760h" # 1 year from creation

  lifecycle {
    ignore_changes = [end_date_relative]
  }
}

