# meta-aurispi

A Yocto/OpenEmbedded BSP layer for building Auris, a high-resolution audio streaming platform.

## Overview

Auris is a dedicated music platform designed to deliver pristine high-resolution audio playback through USB DAC (Digital-to-Analog Converter) devices. The platform provides a web-based interface for managing and streaming your music collection from multiple sources.

## Features

- **High-Resolution Audio Support**: Native playback of Hi-Res audio formats through USB DAC
- **Multiple Audio Sources**:
  - SAMBA/CIFS network shares
  - USB storage devices
  - UPnP/DLNA media servers
- **Web-Based Control**: Intuitive web interface for music library management and playback control
- **Optimized for Audio**: Minimal Linux image focused on audio performance
- **Raspberry Pi Ready**: Built and optimized for Raspberry Pi hardware

## Image

The layer provides `auris-image`, a custom Linux image based on `core-image-minimal` with audio streaming capabilities.

## Architecture

```
┌─────────────────────────────────────┐
│      Web UI (Music Control)         │
├─────────────────────────────────────┤
│     Audio Player Engine              │
├─────────────────────────────────────┤
│  Source Handlers                     │
│  ├─ SAMBA Client                     │
│  ├─ USB Storage Monitor              │
│  └─ UPnP/DLNA Client                 │
├─────────────────────────────────────┤
│     USB DAC Driver                   │
├─────────────────────────────────────┤
│   Linux (Yocto/OpenEmbedded)         │
└─────────────────────────────────────┘
```

## Dependencies

This layer depends on:

```
URI: https://git.openembedded.org/bitbake
    branch: master
    revision: HEAD

URI: https://git.openembedded.org/openembedded-core
    branch: master
    revision: HEAD

URI: https://git.yoctoproject.org/meta-yocto
    branch: master
    revision: HEAD

URI: https://git.yoctoproject.org/meta-raspberrypi
    branch: master
    revision: HEAD
```

## Quick Start

1. Clone required layers:
   ```bash
   git clone https://git.openembedded.org/openembedded-core
   git clone https://git.yoctoproject.org/meta-yocto
   git clone https://git.yoctoproject.org/meta-raspberrypi
   git clone <meta-aurispi-repository>
   ```

2. Initialize build environment:
   ```bash
   source openembedded-core/oe-init-build-env auris-build
   ```

3. Add this layer to `conf/bblayers.conf` and the dependencies above

4. Configure `conf/local.conf`:
   ```
   MACHINE = "raspberrypi5"
   LICENSE_FLAGS_ACCEPTED = "synaptics-killswitch"
   ```

   Optional: Enable SSH login for remote access and debugging:
   ```
   EXTRA_IMAGE_FEATURES:append = " ssh-server-dropbear allow-empty-password empty-root-password allow-root-login"
   ```

5. Build the image:
   ```bash
   bitbake auris-image
   ```

6. Use bmaptool to copy the generated `.wic.bz2` file to the SD card:
   ```bash
   bmaptool copy tmp/deploy/images/raspberrypi5/auris-image-raspberrypi5.wic.bz2 /dev/sdX
   ```

7. Boot your RPI

## Hardware Requirements

### Minimum Requirements
- Raspberry Pi 5
- 16GB+ microSD card
- USB DAC device
- Network connection (Ethernet or WiFi)

### Recommended Requirements
- Raspberry Pi 5 (8GB RAM)
- 32GB+ microSD card (Class 10/UHS-1 or better)
- High-quality USB DAC
- Gigabit Ethernet connection
- External USB storage for local library

## Usage

### Accessing Web Interface

After booting the Auris image:

1. Connect to the device's IP address via web browser
2. Default URL: `http://auris.local` or `http://<device-ip>`

### Adding Music Sources

#### SAMBA/CIFS Share
1. Navigate to Settings → Sources
2. Add network share with credentials
3. Browse and index your music library

#### USB Storage
1. Insert USB drive with music files
2. Device will auto-detect and mount
3. Library appears in web interface

#### UPnP/DLNA Server
1. Ensure UPnP server is on same network
2. Device auto-discovers UPnP sources
3. Select server from available sources

### Connecting USB DAC

1. Connect USB DAC to Raspberry Pi
2. Device automatically detects and configures DAC
3. Select DAC from audio output settings
4. Start playback

## Supported Audio Formats

- FLAC (up to 24-bit/192kHz)
- WAV (PCM)
- AIFF
- ALAC (Apple Lossless)
- DSD (DSF/DFF)
- MP3, AAC (for compatibility)

## Development

### Adding Custom Recipes

Place custom recipes in appropriate subdirectories:
```
meta-aurispi/
├── recipes-auris/
│   ├── audio-player/
│   ├── web-interface/
│   └── source-handlers/
├── recipes-core/
│   └── images/
└── conf/
```

### Contributing

Contributions are welcome! Please follow these guidelines:
- Use clear commit messages
- Test builds before submitting
- Document new features
- Follow Yocto/OE coding standards

## License

This layer is licensed under the MIT License. See `COPYING.MIT` for details.

## Support

For issues and feature requests, please use the issue tracker.

## Roadmap

- [ ] Spotify Connect integration
- [ ] Tidal/Qobuz streaming support
- [ ] Multi-room audio synchronization
- [ ] DSP effects and equalizer
- [ ] Bluetooth audio output
- [ ] Mobile app control
- [ ] Gapless playback
- [ ] Playlist management
- [ ] Album art display

## Acknowledgments

Built with:
- [Yocto Project](https://www.yoctoproject.org/)
- [OpenEmbedded](https://www.openembedded.org/)
- [Raspberry Pi](https://www.raspberrypi.org/)
