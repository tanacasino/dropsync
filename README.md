# dropsync #

Simple sync tools for Dropbox written in scala

## How to use ##

```
# TODO not implemented.
# Start to web auth flow and acquire token to call dropbox core api
# Before start this. Create app in developer page (https://www.dropbox.com/developers/apps)
$ java -jar dropsync.jar setup
# => $HOME/.dropsync.conf
# => accessToken = xxxxx

# SRC_DIR : MUST specified local directory absolute path
# DEST_DIR: MUST specified dropbox directory absolute path (aka. /Downloads, /Public, /Photos)
$ java -jar dropsync.jar sync SRC_DIR DEST_DIR

# Examples
$ java -jar dropsync.jar sync /Users/tanacasino/Downloads /Downloads
$ java -jar dropsync.jar sync /Users/tanacasino/Projects/siteA_html /Public/siteA
```
