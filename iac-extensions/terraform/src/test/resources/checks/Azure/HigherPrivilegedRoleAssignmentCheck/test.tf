# Name base role definition
resource "azuread_directory_role" "privileged-role-administrator" {
  # Noncompliant@+1 {{Make sure that assigning the Privileged Role Administrator role is safe here.}}
  display_name = "Privileged Role Administrator"
}

# Assign the "Privileged Role Administrator" role
resource "azuread_directory_role_member" "privileged-role-administrator-membership" {
  role_object_id   = azuread_directory_role.privileged-role-administrator.object_id
  member_object_id = data.azuread_user.user.object_id
}

# Template id based role definition
resource "azuread_directory_role" "groups-administrator" {
  # Noncompliant@+1 {{Make sure that assigning the Application Administrator role is safe here.}}
  template_id = "9b895d92-2cd3-44c7-9d02-a6ac2d5ea5c3"
}

# Assign the "Groups Administrator" role
resource "azuread_directory_role_member" "groups-administrator-membership" {
  role_object_id   = azuread_directory_role.groups-administrator.object_id
  member_object_id = data.azuread_user.user.object_id
}

# Assign an unknown role role
resource "azuread_directory_role_member" "privileged-role-administrator-membership" {
  role_object_id   = azuread_directory_role.unknown_role.object_id
  member_object_id = data.azuread_user.user.object_id
}

# Duplicate assignment of higher privileged rule
resource "azuread_directory_role_member" "privileged-role-administrator-membership-duplicate" {
  role_object_id   = azuread_directory_role.privileged-role-administrator.object_id
  member_object_id = data.azuread_user.user.object_id
}

# Unused name base role definition
resource "azuread_directory_role" "privileged-role-administrator-unused" {
  display_name = "Privileged Role Administrator"
}

# Unused template id based role definition
resource "azuread_directory_role" "groups-administrator-unused" {
  template_id = "fdd7a751-b60b-444a-984c-02652fe8fa1c"
}

# Template id based role definition without name label
resource "azuread_directory_role" {
  template_id = "9b895d92-2cd3-44c7-9d02-a6ac2d5ea5c3"
}

resource "other_resource" "coverage" {
}

# Missing reference property
resource "azuread_directory_role_member" "privileged-role-administrator-membership-duplicate" {
  member_object_id = data.azuread_user.user.object_id
}

# No object_id reference
resource "azuread_directory_role_member" "privileged-role-administrator-membership-duplicate" {
  role_object_id   = azuread_directory_role.privileged-role-administrator.foo
  member_object_id = data.azuread_user.user.object_id
}

# No attribute access reference
resource "azuread_directory_role_member" "privileged-role-administrator-membership-duplicate" {
  role_object_id   = "foo_bar".object_id
  member_object_id = data.azuread_user.user.object_id
}
