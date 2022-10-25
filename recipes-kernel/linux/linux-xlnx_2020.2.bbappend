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

# most xilinx embedded examples uses OpenCL in the host side to commucate with the FPGA side.
# Thus, the zocl driver is requried
SRC_URI += "file://opencl.cfg \
"

#0001 Disable runtime frequency scaling in dl tasks. This is usefull for power analysis
#0002 Set rt class priority higher than dl. This is usefull to set fred-server w higher priority than sced_deadline tasks
SRC_URI += "file://0001-Disable-runtime-frequency-scaling-in-dl-tasks.patch \
file://0002-Set-rt-class-priority-higher-than-dl.patch \
"

#KERNEL_FEATURES_append = " rt-frag.cfg"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

# it is also possible to apply an complete config with this code
#SUMMARY = "RETIS Lab kernel tweaks - using a complete config file"
#LICENSE = "MIT"

#SRC_URI += "file://defconfig"
#FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
