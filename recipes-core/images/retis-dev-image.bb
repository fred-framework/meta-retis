SUMMARY = "RETIS Lab development image"
LICENSE = "MIT"

inherit core-image
require retis-image.bb

# features to be included
IMAGE_FEATURES += "ssh-server-dropbear tools-debug debug-tweaks"
# see recipes-core/packagroups files for this definition
CORE_IMAGE_EXTRA_INSTALL += "retis-packagegroup-testing"

IMAGE_INSTALL_append = " \
    cmake \
    vim \
"

# this allows to install RPM packages saved in tmp/deploy/rpm using
# $ dnf install package.rpm
EXTRA_IMAGE_FEATURES += "package-management"
