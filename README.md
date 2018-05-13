# Conversations Push Proxy
A [XEP-0357: Push Notifications](https://xmpp.org/extensions/xep-0357.html) app server that relays push messages between the user’s server and Googles Firebase Cloud Messaging.

### Build from source
```
git checkout https://github.com/iNPUTmice/p2.git
cd p2
mvn packege
```

### Install
```
cp target/p1-0.1.jar /opt
cp p2.conf.example /etc/p2.conf
mkdir /var/lib/p2
useradd -d /var/lib/p2 -r p2
chown -R p2:p2 /var/lib/p2
cp dist/p2.service /etc/systemd/system
systemctl daemon-reload
systemctl enable p2.service
systemctl start p2.service
```
