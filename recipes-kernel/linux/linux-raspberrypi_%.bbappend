FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

DEPENDS:append = " pahole-native"

# Set pahole path for BTF support
PAHOLE = "${STAGING_BINDIR_NATIVE}/pahole"
export PAHOLE
EXTRA_OEMAKE:append = " PAHOLE=${PAHOLE}"

SRC_URI:append = " file://usb-audio.cfg \
                   file://initramfs.cfg \
                   file://bpf.cfg \
"

# Symlink Image to Image-initramfs so bootimg-partition uses the bundled kernel
do_deploy:append() {
    if [ "${INITRAMFS_IMAGE_BUNDLE}" = "1" ]; then
        cd ${DEPLOYDIR}
        rm -f Image-${MACHINE}.bin Image
        ln -sf Image-initramfs-${MACHINE}.bin Image-${MACHINE}.bin
        ln -sf Image-initramfs-${MACHINE}.bin Image
    fi
}
