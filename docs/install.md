## Installation

### Build from source
```
git clone https://github.com/iNPUTmice/p2.git
cd p2
mvn package
```

### Manual Install
```
cp target/p2-0.3.jar /opt
mkdir /etc/p2
cp p2.conf.example /etc/p2/config.json
mkdir /var/lib/p2
useradd -d /var/lib/p2 -r p2
chown -R p2:p2 /var/lib/p2
cp dist/p2.service /etc/systemd/system
systemctl daemon-reload
systemctl enable p2.service
systemctl start p2.service
```

There is currently no way to reload the configuration file at runtime but you can always restart the service with `systemctl restart p2.service`.

### Database

Since version 0.3 the Conversations Push Proxy requires a database (drivers for MariaDB are included by default but you can easily change `pom.xml` to include other drivers as well.)

The necessary tables will be created automatically on first use so you just have to create a database and a user. You can do this by starting `mysql` and typing:
```sql
create user 'p2'@'localhost' identified by 'secret';
grant all privileges on p2.* to 'p2'@'localhost';
```

## Configuration of FCM, XMPP Server, and Conversations

### Firebase Cloud Messaging (FCM)

Navigate to the [Firebase Console](https://console.firebase.google.com), create a project, add cloud messaging to the project and create
an Android app.

You will then need to copy three things from the console:

1. the `service account JSON file` and reference it in `p2.conf` as `fcm/serviceAccountFile`.
2. the sender-id and use it for `push.xml` as `gcm_defaultSenderId`.
3. the app-id and use it for `push.xml` as `google_app_id`.

### XMPP Server

You need to configue your xmpp-server to incorporate the push app server p2 as an external component.
Example for ejabberd given below:

```yaml
  ##
  ## ejabberd_service: Interact with external components (transports, ...)
  ##
  -
    port: 5347
    module: ejabberd_service
    access: all
    shaper_rule: fast
    ip: "127.0.0.1"
    hosts:
      "p2.yourserver.tld":
        password: "verysecure"

```

### Building Conversations

Create a file `src/conversationsPlaystore/res/values/push.xml` with the following contents and execute `./gradlew assembleConversationsPlaystoreDebug`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
  <string name="app_server">p2.yourserver.tld</string>
  <string name="gcm_defaultSenderId" translatable="false">copyfromapiconsole</string>
  <string name="google_app_id">1:copyfromapiconsole:android:copyfromapiconsole</string>
  <string name="project_id" translatable="false">copyfromapiconsole</string>
  <string name="google_api_key" translatable="false">copyfromapiconsole</string>
</resources>
```
