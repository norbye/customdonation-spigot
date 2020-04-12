# CustomDonation
Spigot plugin to execute commands received from donation-website through an API.

Sends continous requests to the web API to look for new registered donations, then executes the commands and sends 
another request to the API to register the donation as fulfilled.

## Configuration
```yaml
# Custom Donation vx.x.x
config-version: 1
debug: boolean | false
api:
  root: https://example.com/api.php     # base api url
  poll-interval: 5                      # x seconds to poll the web API
  pass-key: ''                          # simple verification
```

## Commands
`/customdonation reload` - Reload the config and affect the changes

## Setup
### Plugin
1. Make sure the web api is setup
2. Download the jar & dependencies or package them with maven from this code
3. Import the plugin .jar into you plugin folder
4. Include the /lib folder into your plugin folder
5. Run the plugin to generate the configuration
6. Configure it to match your donation API
7. Restart the server or run /customdonation reload
### Web api
API v1

Both endpoints support a simple authentication in form of a header variable sent with the requests.
Auth can be chosen whether to use or not. `HTTP_PASS_KEY` matches `api.pass-key` from the plugin.conf

```dtd
PHP:
$passKeyValue = $_SERVER['HTTP_PASS_KEY'];
```

#### Required endpoints

##### GET $apiRoot
List of current donations that have not yet been executed
```dtd
{
  "donations": {
    [
      "id": 1,
      "username": "eybro",
      "commands": ["op eybro"]
    ], ...
  }
}
```
##### PATCH $apiRoot/:id
Called to verify that the donation has been verified, and thus removed from the donation list of GET apiRoot
```dtd
{
  "success": true
}
```