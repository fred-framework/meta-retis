SUMMARY = "RETIS Lab development image"
LICENSE = "MIT"

inherit core-image
require retis-image.bb

# features to be included
IMAGE_FEATURES += "ssh-server-openssh tools-debug debug-tweaks"
# see recipes-core/packagroups files for this definition
CORE_IMAGE_EXTRA_INSTALL += " \
    retis-packagegroup-testing \
    packagegroup-core-buildessential \
"

# defined in components/yocto/layers/core/meta/recipes-core/packagegroups/packagegroup-core-sdk.bb
#RDEPENDS_${PN} = " \ 
#" 

IMAGE_INSTALL_append = " \
    cmake \
    git \
    vim \
    less \
    ldd \
    file \
    openssh \
    gzip \
    wget \
    dtc \
    coreutils \
"
# might be usefull
# kernel-dev kernel-devsrc kernel-modules

# this allows to install RPM packages saved in tmp/deploy/rpm using
# $ dnf install package.rpm
EXTRA_IMAGE_FEATURES += "package-management"

IMAGE_FEATURES_remove += " ssh-server-dropbear"

