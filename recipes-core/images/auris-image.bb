require recipes-core/images/core-image-minimal.bb

IMAGE_INSTALL:append = " \
    alsa-lib \
    alsa-utils \
    alsa-config \
"
