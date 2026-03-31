terraform {
  required_version = ">= 1.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 4.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.5"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "main" {
  name     = "${var.project_name}-${var.environment}-rg"
  location = var.location

  tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }
}

module "oracle_db" {
  source = "./modules/oracle-db"

  project_name         = var.project_name
  environment          = var.environment
  location             = var.location
  resource_group_name  = azurerm_resource_group.main.name
  oracle_admin_password = var.oracle_admin_password
}

module "aks" {
  source = "./modules/aks"

  project_name        = var.project_name
  environment         = var.environment
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  node_count          = var.aks_node_count
  vm_size             = var.aks_vm_size
}
