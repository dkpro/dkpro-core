## Readme (Import)

These short guidelines will show you how to get a local copy of the dkpro page template and how to correctly import it into a special branch used for github websites (gh-pages). At the end you will have 

- (on github) a branch **gh-pages** where the website for your project is stored
- (locally) a seperate directory for the gh-pages branch of your project which (initially) includes the files of the dkpro-page-template

Below, replace YourProjectName with... your project name (e.g. dkpro-csniper, dkpro-statistics, ...)

- cd into the directory where all your local git repositories are located
- clone project repo into separate directory (here named YourProjectName-page)
	- `git clone https://github.com/dkpro/YourProjectName.git YourProjectName-page`
- add dkpro-page-template repo as additional remote
	- `cd YourProjectName-page`
	- `git remote add dkpro-page-template https://github.com/dkpro/dkpro-page-template.git`
- fetch remote branch
	- `git remote update`
- create and switch to orphaned gh-pages branch
	- `git checkout --orphan gh-pages`
- locally remove leftover files from master branch
	- `git rm -rf .`
- merge template into project gh-pages branch
	- `git merge dkpro-page-template/gh-pages`
- push locally created branch to your project repo
	- `git push -u origin gh-pages`
- make some changes to the page
	- see [ModifyPage.md](ModifyPage.md)
