package main

import (
	"fmt"
	"os"

	"github.com/zalando/zally/cli/zally/domain"
)

func main() {
	app := CreateApp()

	err := app.Run(os.Args)
	if err != nil {
		fmt.Println(err)
		if aerr, ok := err.(domain.Error); ok {
			os.Exit(aerr.Code())
		}
		panic("no error code defined")
	}
}
