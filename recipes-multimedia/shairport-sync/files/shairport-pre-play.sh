#!/bin/sh
# Shairport Sync pre-play hook script
# Executes before AirPlay audio playback begins
# Stops MPD to ensure exclusive access to audio device

logger -t shairport-sync "AirPlay connection detected, stopping MPD and upmpdcli"

# Stop MPD and its UPnP frontend to release audio device
systemctl stop mpd-auris.service 2>/dev/null
systemctl stop upmpdcli.service 2>/dev/null

exit 0
