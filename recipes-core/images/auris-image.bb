require recipes-core/images/core-image-minimal.bb

SUMMARY = "Auris audio image"

IMAGE_INSTALL:append = " \
    alsa-lib \
    alsa-utils \
    alsa-config \
    cpu-performance \
    mpd \
    mpd-auris \
    upmpdcli \
    shairport-sync \
    avahi-daemon \
    libconfig \
"
