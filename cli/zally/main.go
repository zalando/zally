package main

import (
	"os"
)

func main() {
	app := CreateApp()

	err := app.Run(os.Args)
	if err != nil {
		os.Exit(1)
	}
}
