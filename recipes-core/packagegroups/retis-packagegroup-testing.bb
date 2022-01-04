DESCRIPTION = "RETIS Lab test application packagegroup"
SUMMARY = "RETIS Lab packagegroup - tools/testapps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} = " \
    ethtool \
    evtest \
    fbset \
    i2c-tools \
    memtester \
    strace \
    perf \
"