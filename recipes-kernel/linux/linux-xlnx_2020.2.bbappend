SUMMARY = "RETIS Lab kernel tweaks - using a fragment file"
LICENSE = "MIT"

# the fragment extension must be .cfg. You can add as many fragment as you want
# remove preempt_rt.cfg and patch-5.4.3-rt1.patch if you dont want to have preempt_rt patch
SRC_URI += "file://rt-frag.cfg \
file://preempt_rt.cfg \
file://patch-5.4.3-rt1.patch \
"

# cpufreq userspace governor is disabled by default
# this governor enables the user to control dvfs
# https://www.kernel.org/doc/html/latest/admin-guide/pm/cpufreq.html?highlight=schedutil#userspace
SRC_URI += "file://cpufreq.cfg \
"

#KERNEL_FEATURES_append = " rt-frag.cfg"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

# it is also possible to apply an complete config with this code
#SUMMARY = "RETIS Lab kernel tweaks - using a complete config file"
#LICENSE = "MIT"

#SRC_URI += "file://defconfig"
#FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
