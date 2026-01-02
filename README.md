# meta-aurispi

![Auris Logo](logo.png)

A Yocto/OpenEmbedded BSP layer for building Auris, a high-resolution audio streaming platform.

## Overview

Auris is a dedicated music platform designed to deliver pristine high-resolution audio playback through USB DAC (Digital-to-Analog Converter) devices. The platform provides a web-based interface for managing and streaming your music collection from multiple sources.

## Features

- **High-Resolution Audio Support**: Native playback of Hi-Res audio formats through USB DAC
- **RAM Boot System**: Entire OS runs from RAM (initramfs), no rootfs partition needed
  - Minimal SD card writes for extended lifespan
  - Fast boot and shutdown
  - Optimized for audio-focused workloads
- **AirPlay Support**: Stream audio from iOS, macOS, and iTunes via Shairport Sync with automatic switching
- **UPnP Renderer**: Act as UPnP/DLNA audio renderer to receive audio streams from UPnP control points
- **Multiple Audio Sources**:
  - AirPlay (from iOS/macOS devices)
  - SAMBA/CIFS network shares
  - USB storage devices
  - UPnP/DLNA media servers
- **Bit-Perfect Audio**: Direct hardware access with no sample rate conversion
- **Real-Time Audio Scheduling**: Dynamic CPU scheduling with FIFO priorities
  - mpd-auris (Backend): FIFO priority 80 (ALSA audio control - critical path)
  - upmpdcli (UPnP Renderer): FIFO priority 75 (primary playback - depends on mpd)
  - Shairport Sync (AirPlay): FIFO priority 70 (independent ALSA access)
- **Web-Based Control**: Intuitive web interface for music library management and playback control
- **Optimized for Audio**: Minimal Linux image focused on audio performance
- **Raspberry Pi Ready**: Built and optimized for Raspberry Pi 5 hardware

## Image

The layer provides `auris-image`, a custom Linux image based on `core-image-minimal` with audio streaming capabilities.

### Image Architecture

- **RAM Boot System**: Entire OS runs from RAM via initramfs (mounted on `/dev/ram0`)
- **Single Boot Partition**: No separate rootfs partition, minimizes SD card usage
- **Kernel with Embedded Initramfs**: Kernel built with embedded auris-initramfs-image
- **WIC Image Format**: Pre-partitioned `.wic.bz2` format for easy SD card deployment
- **Audio-Optimized**: Minimal rootfs focused on audio performance

## Architecture

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────┐
│                      Network Sources                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ UPnP Server  │  │  AirPlay     │  │ SAMBA/USB    │     │
│  │  (Primary)   │  │              │  │  (Local)     │     │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘     │
└─────────┼──────────────────┼──────────────────┼────────────┘
          │                  │                  │
    ┌─────▼─────────────┐    │          ┌───────▼────┐
    │ upmpdcli (UPnP)   │    │          │ File/USB   │
    │ FIFO: 75          │    │          │ Browser    │
    │ (Primary playback)│    │          └────────────┘
    └────────┬──────────┘    │
             │                │     ┌──────────────────┐
             │                │     │ Shairport-sync   │
             │                └────▶│ FIFO: 70         │
             │                      │ (AirPlay)        │
             │                      │ Direct ALSA      │
    ┌────────▼─────────────────┐    └────────┬─────────┘
    │   mpd-auris (Backend)    │             │
    │   FIFO: 80 (Critical)    │             │
    │   ALSA Control & Output  │             │
    └────────┬─────────────────┘             │
             │                                │
    ┌────────┴────────────────────────────────┴──┐
    │            USB DAC Audio Output             │
    │         (Bit-Perfect, No Resampling)        │
    └──────────────────────────────────────────────┘

CPU Scheduler (scx_bpfland):
    Primary Domain: CPU 3 (Audio)
    Overflow Domain: CPU 0-2 (System Services)

    FIFO Priority Hierarchy:
    80: mpd-auris ────────── Critical ALSA Control
    75: upmpdcli ─────────── Depends on mpd-auris
    70: Shairport-sync ───── Independent ALSA
    Normal: Other Services

Interrupt Handling: CPU 0-2 (irqaffinity=0-2)
```

For a detailed explanation of the architecture and design principles, see [meta-aurispi: HiFi Music Streaming Platform](https://pcjustin.com/2025/12/13/2025-12-13-meta-aurispi-hifi-music-streaming-platform/)

### Audio Signal Flow

**UPnP Playback (Primary):**
```
UPnP Server → upmpdcli (control) → mpd-auris (ALSA) → USB DAC
```

**AirPlay Playback (Independent):**
```
AirPlay Source → Shairport-sync (direct) → USB DAC
```

Both use the same USB DAC but independent paths to avoid conflicts.

### Real-Time Scheduling Strategy

- **mpd-auris** (FIFO 80): Highest priority, controls critical ALSA interface
- **upmpdcli** (FIFO 75): Secondary priority, depends on mpd-auris for audio output
- **Shairport-sync** (FIFO 70): Independent ALSA access, lower priority
- **System Services**: Normal priority, dynamically scheduled by scx_bpfland

Priority inversion is avoided because mpd-auris (critical path) has highest priority among audio applications.

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

URI: https://git.openembedded.org/meta-openembedded
    branch: master
    revision: HEAD
    layers: meta-oe, meta-python, meta-multimedia
```

## Quick Start

1. Clone required layers:
   ```bash
   git clone https://git.openembedded.org/openembedded-core
   git clone https://git.yoctoproject.org/meta-yocto
   git clone https://git.yoctoproject.org/meta-raspberrypi
   git clone https://git.openembedded.org/meta-openembedded
   git clone <meta-aurispi-repository>
   ```

2. Initialize build environment:
   ```bash
   source openembedded-core/oe-init-build-env auris-build
   ```

3. Add layers to `conf/bblayers.conf`:
   - meta-aurispi
   - meta-openembedded/meta-oe
   - meta-openembedded/meta-python
   - meta-openembedded/meta-multimedia

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

### Using AirPlay

Stream audio directly from your iOS, macOS, or iTunes device:

1. Ensure the device is on the same network as the Auris system
2. On your Apple device, open the AirPlay menu:
   - **iOS/iPadOS**: Control Center → Music playback card → AirPlay icon
   - **macOS**: Click the volume icon in the menu bar → AirPlay
   - **iTunes**: Click the AirPlay icon in the lower right
3. Select "Auris Audio" from the available AirPlay devices
4. Start playing audio - it will stream directly to the USB DAC

**Note**: When AirPlay is active, MPD and UPnP services are automatically paused. They will resume when you disconnect from AirPlay.

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

### Initramfs RAM Boot

The system boots from initramfs (initial RAM filesystem) for optimized audio performance:

#### Configuration
- **Initramfs Image**: `auris-initramfs-image` - custom minimal rootfs with audio packages
- **Kernel Bundling**: Initramfs is embedded directly in kernel via `INITRAMFS_IMAGE_BUNDLE = "1"`
- **Root Filesystem**: Mounted as `/dev/ram0` at boot time
- **Kernel Parameters**: `root=/dev/ram0` in cmdline.txt
- **Configuration Files**:
  - `conf/layer.conf` - Initramfs image and bundling settings
  - `wic/sdimage-auris-initramfs.wks` - Single partition WIC template
  - `recipes-kernel/linux/linux-raspberrypi_%.bbappend` - Kernel config files

#### Benefits
- Reduced SD card wear (all writes to RAM)
- Fast boot/shutdown cycles
- Consistent audio performance without I/O interference

### USB DAC Audio Configuration

#### Hardware Setup
- USB DAC automatically detected as primary audio device
- ALSA interface available at `/proc/asound`
- Bluetooth and WiFi disabled to minimize interference

#### Configuration
- **Device Tree Overlays**:
  - `dtoverlay=vc4-kms-v3d,noaudio` - HDMI output with audio disabled
  - `dtoverlay=disable-bt-pi5` - Disable Bluetooth
  - `dtoverlay=disable-wifi-pi5` - Disable WiFi
  - Located in: `recipes-bsp/bootfiles/rpi-config_git.bbappend`
- **Kernel Audio Support**:
  - `CONFIG_SND_USB_AUDIO=m` - USB Audio driver (compiled as module)
  - Located in: `recipes-kernel/linux/files/usb-audio.cfg`

### System Performance Tuning

The Auris platform implements several performance optimizations:

#### System Startup Order
The services are carefully configured to avoid circular dependencies:

```
Boot → network-online → scx-bpfland → upmpdcli → mpd-auris
                                    ↓
                            shairport-sync
```

**Service Dependencies:**
- **scx-bpfland**: Starts after `network-online.target` (not `multi-user.target`)
  - Provides dynamic CPU scheduling via BPF
  - Must start early to manage system load
- **mpd-auris**: Started as a dependency of `upmpdcli`
  - FIFO priority 80 (critical audio control)
  - Configured with `WantedBy=upmpdcli.service`
  - Depends on `scx-bpfland.service`
- **upmpdcli**: Starts with `multi-user.target`
  - Automatically pulls in `mpd-auris` via `Wants=mpd-auris.service`
  - FIFO priority 75 (primary playback)
  - Depends on `mpd-auris.service`

This ordering prevents the circular dependency that would occur if both services were directly tied to `multi-user.target`.

#### CPU Isolation & Scheduling
- **sched_ext Scheduler (scx_bpfland)**:
  - Dynamic BPF-based scheduler for intelligent task distribution
  - Primary domain: CPU 3 (--primary-domain 0x8)
  - CPU 3 prioritized for audio applications (mpd-auris, upmpdcli, shairport-sync)
  - System services dynamically scheduled by scx_bpfland
  - Automatically balances load: high-priority audio tasks on CPU 3, others overflow to CPUs 0-2
  - Located in: `recipes-kernel/scx/files/scx-bpfland.service`
- **Interrupt Affinity**: `irqaffinity=0-2`
  - Keeps interrupts on CPUs 0-2
  - Protects CPU 3 from interrupt latency
  - Located in: `recipes-bsp/bootfiles/rpi-cmdline.bbappend`

#### Real-Time Process Scheduling
- **mpd-auris**: FIFO priority 80 - Music Player Daemon (critical ALSA control point)
- **upmpdcli**: FIFO priority 75 - UPnP/DLNA renderer (primary playback method, depends on mpd)
- **Shairport Sync**: FIFO priority 70 - AirPlay audio receiver (independent ALSA access)
- CPU scheduling: Dynamically managed by scx_bpfland based on FIFO priorities and system load
- Priority inversion prevention: Critical path (mpd-auris) has highest priority

#### CPU Governor
- Set to "performance" to minimize frequency scaling latency
- Applied via `cpu-performance.service`

#### ALSA Configuration
- Mixer disabled for direct hardware access
- Zero-copy audio buffer management
- Located in: `recipes-multimedia/alsa/alsa-config/asound.conf`

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

- [x] RAM boot support (initramfs-based system)
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
