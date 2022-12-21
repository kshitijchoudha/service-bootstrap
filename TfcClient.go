package main

import (
	"context"
	"log"
	"os"

	tfe "github.com/hashicorp/go-tfe"
	valast "github.com/hexops/valast"
)

func main() {
	config := &tfe.Config{
		Token: os.Getenv("TFC_TOKEN"),
	}

	client, err := tfe.NewClient(config)
	if err != nil {
		log.Fatal(err)
	}

	// Create a context
	ctx := context.Background()

	var orgName = "example-org-7e8de7"
	var workSpaceName = "eks-deployment"
	_ = workSpaceName

	//deleteWorkSpace(ctx, client, workSpaceName, orgName)
	//createWorkSpace(ctx, client, workSpaceName, orgName)
	updateAWSToken(ctx, client, orgName)
}
func deleteWorkSpace(ctx context.Context, client *tfe.Client, workSpaceName string, orgName string) {

	// Create a new workspace
	err := client.Workspaces.Delete(ctx, orgName, workSpaceName)
	if err != nil {
		log.Fatal(err)
	}
}

func createWorkSpace(ctx context.Context, client *tfe.Client, workSpaceName string, orgName string) {

	// Create a new workspace
	_, err := client.Workspaces.Create(ctx, orgName, tfe.WorkspaceCreateOptions{
		Name: tfe.String(workSpaceName),
	})
	if err != nil {
		log.Fatal(err)
	}
}

func updateAWSToken(ctx context.Context, client *tfe.Client, orgName string) {

	varSetList, err := client.VariableSets.List(ctx, orgName, nil)
	println((valast.String(varSetList)))
	if err != nil {
		log.Fatal(err)
	}

	varList, err := client.VariableSetVariables.List(ctx, varSetList.Items[0].ID, nil)
	println((valast.String(varList)))

	if err != nil {
		log.Fatal(err)
	}
}

func updateWorkSpace() {
	// Update the workspace
	// w, err = client.Workspaces.Update(ctx, "example-org-7e8de7", w.Name, tfe.WorkspaceUpdateOptions{
	// 	AutoApply:        tfe.Bool(false),
	// 	TerraformVersion: tfe.String("0.11.1"),
	// 	WorkingDirectory: tfe.String("my-app/infra"),
	// })
	// if err != nil {
	// 	log.Fatal(err)
	// }
}
