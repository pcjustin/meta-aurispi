#!/bin/sh
# Shairport Sync post-play hook script
# Executes after AirPlay audio playback ends
# Restores MPD and its UPnP frontend

logger -t shairport-sync "AirPlay disconnected, restoring MPD and upmpdcli"

# Restore MPD and its UPnP frontend
systemctl start mpd-auris.service 2>/dev/null
systemctl start upmpdcli.service 2>/dev/null

exit 0
