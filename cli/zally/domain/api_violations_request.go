package domain

// APIViolationsRequest is a wrapper around API definition
type APIViolationsRequest struct {
	APIDefinitionRaw string `json:"api_definition_raw"`
}
