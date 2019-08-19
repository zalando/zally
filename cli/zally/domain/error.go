package domain

const (
	ServerError = iota + 1
	ClientError
	ValidationError
)


type Error interface {
	Code() int
}

type AppError struct {
	e error
	c int
}

func (e *AppError) Error() string {
	return e.e.Error()
}

func (e *AppError) Code() int {
	return e.c
}

func NewAppError(e error, c int) error {
	return &AppError{e: e, c: c}
}
