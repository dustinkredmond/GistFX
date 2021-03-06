
## GistFX

Forget using your default text editor application to jot down pieces of
code. Why not store these, instead, in a version controlled format?

GitHub Gists are a great way to store small code snippets, or even small
projects. GistFX provides a desktop Gist client, to make editing, creating, and
manipulating Gists a breeze.

### Features
  - Easily manage existing Gists, edit, or create new ones
  - Built-in syntax highlighting for most modern languages
  - Authenticate with an access token, no trusting a stranger's code
    with your GitHub username/password.
    - Only give the access token the scope that it needs (i.e. view/edit Gists)
### Getting started

  1. Create a GitHub Personal Access Token for this application
     - This is far superior to user/pass authentication
     - Only gives the application the permissions that it needs
       - Create by going to: GitHub -> Settings -> Developer Settings -> Personal access tokens -> Generate new token
         - [Or click here](https://github.com/settings/tokens/new)
       - Make sure to click the checkbox next to `gist` scope
    
  2. Build or download
     - Build GistFX with Apache Maven 
        - Simply clone the repository, `cd` into the directory, then
        run `mvn clean package`. This will generate a runnable JAR file
        in the GistFX/target directory. Simply double-click to run.
     - Download a copy from GitHub Releases (found to the right of the repo), then
     double-click to run
---

### On first run

You will be presented with a login screen, enter the GitHub personal 
access token created in step one, above.


 ![Login image](./img/GistFX-Login.png) 
 

If you click the `Save Access Token` checkbox, GistFX will save this info locally
on your PC so that you do not have to enter a token next time.

---

Changes you make in GistFX will be reflected on GitHub. You can also conveniently access
Gists or files within Gists on GitHub from the right-click context menu available
on GistFX table views and lists.