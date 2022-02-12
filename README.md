# meta-retis

Yocto layer/image from [Real-Time Systems Laboratory (ReTiS Lab)](https://retis.santannapisa.it/), [Scuola Superiore Sant'Anna (SSSA)](https://www.santannapisa.it/), Pisa, Italy.

## Requirements

This documentation considers the following requirements:
 - PetaLinux v2020.2;
 - ZCU102 UltraScale+ board;

Despite of that, we *believe* that this Yocto Layer is general enough, with minor tweaks, to be used with other Petalinux versions and Xilinx boards. For testing purposes, we checked the described flow with a PYNQ-Z1 board and the image worked. See the section `Working with Other Boards` for more information.

## Creating a PetaLinux Project and Basic Configuration

Create a PetaLinux project using the following command:
```
$ petalinux-create -t project -s <path to the xilinx-zcu102-v2020.2-final.bsp>
```
Reconfigure the project with the hardware design (.xsa or .bsp):
```
$ cd xilinx-zcu102-2020.2
$ petalinux-config --get-hw-description=<path containing edt_zcu102_wrapper.xsa>
```
In this case, one can use either the [default project](https://xilinx.github.io/Embedded-Design-Tutorials/docs/2020.2/docs/Introduction/ZynqMPSoC-EDT/3-system-configuration.html) or a custom vivado design. 

```
$ cd <proj-dir>
$ petalinux-config
    Subsystem AUTO Hardware Settings  ---> Ethernet Settings  ---> Randomise MAC address
    Image Packaging Configuration  ---> Root filesystem type ---> EXT4 SD card
```

This previous command run petalinux-config and it does two basic configurations. The first configuration is to randomize the Ethernet MAC address. This is important when there are multiple petalinux boards in the same network because, otherwise, there will be connection problems among the boards. The second configuration configure the board to boot from the SD card with EXT4 partition. 
This process will create the build directory in the newly created petalinux project. The configuration is saved in the file `./project-spec/configs/config` and it looks like this:

```
CONFIG_SUBSYSTEM_ETHERNET_PSU_ETHERNET_3_MAC_PATTERN="00:0a:35:00:??:??"
# CONFIG_SUBSYSTEM_ROOTFS_INITRD is not set
# CONFIG_SUBSYSTEM_ROOTFS_JFFS2 is not set
# CONFIG_SUBSYSTEM_ROOTFS_NFS is not set
CONFIG_SUBSYSTEM_ROOTFS_EXT4=y
# CONFIG_SUBSYSTEM_ROOTFS_OTHER is not set
CONFIG_SUBSYSTEM_SDROOT_DEV="/dev/mmcblk0p2"
CONFIG_SUBSYSTEM_ETHERNET_PSU_ETHERNET_3_MAC="00:0a:35:00:dd:07"
CONFIG_SUBSYSTEM_BOOTARGS_GENERATED=" earlycon console=ttyPS0,115200 clk_ignore_unused root=/dev/mmcblk0p2 rw rootwait"
```

When petalinux-config exits, it creates the *build directory*. It is recommemded to reuse **Yocto downloads and sstates directories** from previous petalinux projects. If these two directories do not exist, no problem. It will only take longer to download all necessary code. For reuse the directories, it is enough to execute the follwoing commands:

```
$ cd build
$ ln -s <yocto-download-dir> downloads
$ ln -s <yocto-sstate-cache> sstate-cache
```

## Including `meta-retis` Layer

The next step is to include this layer into the petalinux project by running:

```
$ cd <proj-dir>
$ mkdir components/ext_source
$ cd components/ext_source
$ git clone -b v2020.2 https://github.com/fred-framework/meta-retis.git
$ cd meta-retis
$ tree
├── conf
│   └── layer.conf
├── LICENSE
├── README.md
├── recipes-apps
│   └── real-time
│       └── stress-ng_0.13.09.bb
├── recipes-bsp
│   └── device-tree
│       ├── device-tree.bbappend_xx
│       └── files
│           └── system-user.dtsi
├── recipes-core
│   ├── images
│   │   ├── retis-dev-image.bb
│   │   ├── retis-image.bb
│   │   └── retis-kernel-dev-image.bb
│   ├── packagegroups
│   │   └── retis-packagegroup-testing.bb
└── recipes-kernel
    └── linux
        ├── files
        │   ├── defconfig
        │   ├── ftrace.cfg
        │   ├── patch-5.4.3-rt1.patch
        │   ├── preempt_rt.cfg
        │   └── rt-frag.cfg
        └── linux-xlnx_2020.2.bbappend
```

Then, in the build directory, run petalinux-config to add the path to the layer.

```
$ cd build
$ petalinux-config
  Yocto Settings  --->  User Layers  ---> 
    ${PROOT}/components/ext_source/meta-retis
```

## Image Customization

This layer extends the default petalinux image called `petalinux-image-minimal`, creating three new images:

 - [`retis-image`](./recipes-core/images/retis-image.bb): Includes the `petalinux-image-minimal` plus editors and htop. Access via serial interface or keyboard/hdmi;
 - [`retis-dev-image`](./recipes-core/images/retis-dev-image.bb): Includes the previous image,  ethernet/wifi, and conventional development tools, e.g., gcc, make, etc. Access via SSH; 
 - [`retis-kernel-dev-image`](./recipes-core/images/retis-dev-image.bb): Includes the previous image, and kernel development tools and tracers;

In [`retis-image.bb`](./recipes-core/images/retis-image.bb), it is possible to change the base petalinux image from `petalinux-image-minimal` (located in `./components/yocto/layers/meta-petalinux/recipes-core/images/petalinux-image-minimal.bb`) to `petalinux-image-full`, located in the same directory. In addition, both development images have comments of possible additional tools to be installed. Please browse these files and find out what is required for your application.

### Kernel Customization

The kernel customization can be done manually by executing this command:

```
$ petalinux-config -c kernel
```

Or, preferably, it can be done automatically using the bbappend of this layer. In this case, run the following command to check whether the kernel bbappend file was detected:

```bash
$ cd <proj-dir>
$ source components/yocto/layers/core/oe-init-build-env 
$ bitbake-layers show-appends | grep retis
...
linux-xlnx_2020.2.bb:
  /ssd/work/petalinux/2020.2/xilinx-zcu102-2020.2/components/ext_source/meta-retis/recipes-kernel/linux/linux-xlnx_2020.2.bbappend
...
```

If the bbappend is listed, then we have a successful initial configuration. Next, we run the kernel compilation, using one of the images mentioned before:

```
$ petalinux-build  -c <image-name> -x compile
```

The kernel configurations files are located at `recipes-kernel/linux/files` and they are activated by including them in its bbappend file. By default, only the `ftrace.cfg` configuration file is not compiled.
 - [`recipes-kernel/linux/files/rt-frag.cfg`](./recipes-kernel/linux/files/rt-frag.cfg)
 - [`recipes-kernel/linux/files/ftrace.cfg`](https://www.kernel.org/doc/html/latest/trace/ftrace.html)
 - [`recipes-kernel/linux/files/preempt_rt.cfg`](./recipes-kernel/linux/files/preempt_rt.cfg)


All the kernel configuration files will be stores in the directory `./components/yocto/workspace/sources/linux-xlnx/oe-local-files/`

The kernel configuration fragments can also be located here `./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git<SOME-NUMBER>/cpu_idle.cfg` and the final configuration file can be found here `./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git<SOME-NUMBER>/linux-xlnx-5.4+git<SOME-NUMBER>/.config`. Check wheter the final configuration file is correct by checking the expected option values. For instance:

```bash
$ cat ./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git<SOME-NUMBER>/linux-zynqmp_generic-standard-build/.config | grep CONFIG_HZ_1000
CONFIG_HZ_1000=y
```

The expected value is `y`. In case of error during the kernel compilation, please check the logs in the directory `./build/tmp/work/zynqmp_generic-xilinx-linux/linux-xlnx/5.4+git<SOME-NUMBER>/temp/`.

Another kernel option set in this example is `CONFIG_IKCONFIG`, which writes the kernel configuration file into `/proc/config.gz`. Thus, one can check the option values by running in the board:

```bash
$ zcat /proc/config.gz | grep <option-name>
```

When running the kernel in the board, it is possible to check whether the kernel options where correctly deployed. For instance, one of the options set in this layer is to put the frequency scaling mode to `performance` by default. In this case, this can be checked with this command:

```bash
$ cat /sys/devices/system/cpu/cpufreq/policy0/scaling_governor 
performance
$ cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor 
performance
performance
performance
performance
```

### PREEMPT_RT Kernel Support

**ATTENTION:** The preempt_rt patch is applied by default. If you don't want it, go to the file [`recipes-kernel/linux/linux-xlnx_2020.2.bbappend`](./recipes-kernel/linux/linux-xlnx_2020.2.bbappend) and remove the files `preempt_rt.cfg` and `patch-5.4.3-rt1.patch` from `SRC_URI`.

The rest of this section documents how the patch was included into the layer, so that the process can be repeated in the future, for supporting a different Linux kernel version. 
The first step to apply preempt_rt patch is to figure out the Linux kernel version used by this version of petalinux (2020.2). These commands show this information:

```
$ cd ./components/yocto/workspace/sources/linux-xlnx/
$ more Makefile
# SPDX-License-Identifier: GPL-2.0
VERSION = 5
PATCHLEVEL = 4
SUBLEVEL = 0
EXTRAVERSION =
NAME = Kleptomaniac Octopus
$ git status
On branch xlnx_rebase_v5.4
```

It shows that we have kernel version v5.4.0. Next, we have to find the preempt_rt patch version with closest match, which in this case is [patch-5.4.3-rt1](https://cdn.kernel.org/pub/linux/kernel/projects/rt/5.4/older/patch-5.4.3-rt1.patch.gz). To apply this patch, execute the following commands:

```
$ cd ./components/yocto/workspace/sources/
$ wget https://cdn.kernel.org/pub/linux/kernel/projects/rt/5.4/older/patch-5.4.3-rt1.patch.gz
$ gunzip patch-5.4.3-rt1.patch.gz
$ cd linux-xlnx/
$ patch --dry-run -p1 < ../patch-5.4.3-rt1.patch | grep -i failed
$ patch -p1 < ../patch-5.4.3-rt1.patch
```
ps.: the kernel source code can also be located in `build/tmp/work-shared/zynqmp-generic/kernel-source`. 

Note that the first patch execution is only used to test if the patch worked. If not, then you have to try patching another patch version or consider replacing the Linux kernel version. This command showed that there was a single error. If we proceed with the process as it is, there will be a failure. 

```
$ cd <proj-dir>
$ petalinux-build -c kernel -x patch
```

The used patch present an error as shown below. Analysing the code, the patch was removing 2 lines that were not anymore in the code, so these modifications could be safely removed manually from the pacth file. 

```
| error: patch failed: arch/arm/kernel/smp.c:678
| error: arch/arm/kernel/smp.c: patch does not apply
```

After repeating the process, the patch was successfull and we could proceeed to kernel compilation and image generations:

```
$ cd <proj-dir>
$ petalinux-build -c kernel -x patch
$ petalinux-build -c kernel -x compile
$ petalinux-build -c <image-name>
```

Finally, proceeed with the boot and test the image with the following commands:

```
root@xilinx-zcu102-2020_2:~# uname -a
Linux xilinx-zcu102-2020_2 5.4.0-rt1-xilinx-v2020.2 #1 SMP PREEMPT_RT Sun Jan 23 14:21:29 UTC 2022 aarch64 GNU/Linux
root@xilinx-zcu102-2020_2:~# zcat /proc/config.gz | grep CPU_IDLE
# CONFIG_CPU_IDLE is not set
root@xilinx-zcu102-2020_2:~# zcat /proc/config.gz | grep PREEMPT_RT
CONFIG_PREEMPT_RT=y
```

### Updating the Device Tree

The device tree must be generated with embedded labels so these labels can be referenced later in the device tree overlays in runtime. To do so, `dtc` must be executed the `-@` argument. This will make dtc retain information about labels when generating a dtb file, which will allow Linux to figure out at runtime what device tree node a label was referring to. To achieve such results in petalinux, the `CONFIG_SUBSYSTEM_DEVICETREE_COMPILER_FLAGS` option must be set. In the base directory run the following command:

```
$ grep -r CONFIG_SUBSYSTEM_DEVICETREE_COMPILER_FLAGS --include="config" .
./project-spec/configs/config:CONFIG_SUBSYSTEM_DEVICETREE_COMPILER_FLAGS="-@"
./pre-built/linux/images/config:CONFIG_SUBSYSTEM_DEVICETREE_COMPILER_FLAGS="-@"
```

If the option is not set as expected, then use petalinux-config to do so.

Both the `retis-dev-image` and `retis-kernel-dev-image` include the device tree compiler `dtc` and its auxiliar programs. For instance, `fdtoverlay` is a tool created to test libfdt. It applies one or more overlay .dtb files to a base .dtb file. It appears that some people are also using fdtoverlay to apply overlays pre-boot.

Finally, the kernel is configured to support device tree overlays. So the designer has all the tools required o tweak the device tree in runtime with [overlay fragments](https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/plain/Documentation/devicetree/overlay-notes.rst).

Currently, to change the device tree, it is necessary to add the device in the 
`project-spec/meta-user/recipes-bsp/device-tree/files/system-user.dtsi` file. Here is an example taken from [here](https://github.com/Xilinx/linux-xlnx/blob/master/Documentation/devicetree/bindings/gpio/gpio-xilinx.txt) and [here](https://xilinx-wiki.atlassian.net/wiki/spaces/A/pages/18842398/Linux+GPIO+Driver) for a gpio device:

```
/include/ "system-conf.dtsi"
/ {
    gpio123: gpio@41000000 {
            #gpio-cells = <2>;
            compatible = "xlnx,xps-gpio-1.00.a";
            gpio-controller ;
            reg = < 0x41000000 0x10000 >;
    };
};
```

When running in the board, it will be possible to see the definition in:

```
$ cat /sys/firmware/devicetree/base/__symbols__/gpio123
```

When the devicetree is changed, run the following commands to update it only:

```
$ petalinux-build -c device-tree -x cleanall
$ petalinux-build -c device-tree
```

## Bootloader Customization

This recipe does not change the bootloader but, if they are required, then run:

```
petalinux-config u-boot
petalinux-build -c u-boot
```

It is also possible to modify the `./project-spec/meta-user/conf/petalinuxbsp.conf` file.

### RootFS Customization

The images already add software to the RootFS. However, it is also possible to browse a *menuconfig style* tool to select addtional tools by running:

```
$ petalinux-config -c rootfs
```

Another way to customize the rootfs is by including recipes for additional packages. For instance, the recipe `recipes-apps/real-time/stress-ng_0.13.09.bb` replaces [the default stress version used in Yocto Zeus](http://cgit.openembedded.org/openembedded-core/tree/meta/recipes-extended/stress-ng/stress-ng_0.10.00.bb?h=zeus) by a newer version. When running in the board, one can confirm that the newer version is used:

```
root@xilinx-zcu102-2020_2:~# stress-ng -v
stress-ng: debug: [838] stress-ng 0.13.09 g757b66b49e4b
stress-ng: debug: [838] system: Linux xilinx-zcu102-2020_2 5.4.0-xilinx-v2020.2 #1 SMP Sat Jan 22 14:31:20 UTC 2022 aarch64
stress-ng: debug: [838] RAM total: 3.8G, RAM free: 3.8G, swap free: 0.0
stress-ng: debug: [838] 4 processors online, 4 processors configured
stress-ng: info:  [838] defaulting to a 86400 second (1 day, 0.00 secs) run per stressor
stress-ng: error: [838] No stress workers invoked
```

When there are multiple versions for the same recipe, like in this example with stress-ng, the following command has to be used to select the version:

```
PREFERRED_VERSION_stress-ng = "0.13.09"
```

If you want to build a recipe with your own software, please refer to [`learning-yocto`](https://github.com/amamory-embedded/learning-yocto).

Now, the image is ready to be built.

## Image Building and Packing

In the `<PetaLinux-project>` directory, build the Linux images using the following command:
```
$ petalinux-build -c <image-name>
```

This last command takes a long time when it is first executed,specially if the Yocto downloads and sstate directories were not reused. After the build process is finished, verify the images in the directory `images/linux`. 

Go to the image directory and generate the boot image `BOOT.BIN` using the following command:

```
$ cd images/linux
$ petalinux-package --boot --fsbl zynqmp_fsbl.elf --u-boot
```

Or executing :

```
$ cd images/linux
$ petalinux-package --boot --force --fsbl zynqmp_fsbl.elf --fpga system.bit --pmufw pmufw.elf --atf bl31.elf --u-boot u-boot.elf
```
If the vivado design uses the **PL** part. Note that this last option includes the .bit into the `BOOT.BIN`. 

The list of installed packages can be found in the `images/linux/rootfs.manifest` file.

## Image Deploy

Copy the `BOOT.BIN`, `image.ub`, and `boot.scr` files to the **boot partition** of the SD card, formated with **FAT32**. Copy the `rootfs.tar.gz` file to the **rootfs partition** of the SD card, formated with **EXT4**. 

```
$ sudo tar -xf rootfs.tar.gz --directory=/media/<user>/<rootfs-partition>
```

An alternative to the standard approach to save the image is to use the **wic image format**. Wic creates a single file with the entire partition tree. This format supports sparse file formats such that, when compressed, their size is greatly reduced even for large partitions. The wic file is generated like this, in the `images/linux` directory:

```
$ cd images/linux
$ petalinux-package --wic
$ ls -lah *wic
  -rw-r--r-- 1 aamory aamory 4.1G Jan 17 19:51 petalinux-sdimage.wic
$ tar czf  petalinux-sdimage.wic.tar.gz petalinux-sdimage.wic
$ ls -lah *wic.tar.gz
  -rw-rw-r-- 1 aamory aamory  48M Jan 17 19:52 petalinux-sdimage.wic.tar.gz
```

Note the reduced file size when compressed.  Wic is a smarter image format that can save alot of time when burning a SD card. Use [balena etcher](https://www.balena.io/etcher/) or [bmaptool](https://github.com/intel/bmap-tools) for faster results. 

First, insert the SD card and run `df` to find out the SD card device (assuming it is `/dev/sdb`).
Next, unmount the its partitions. If `bmaptool` is not installed, run: 

```bash
$ sudo apt install bmap-tools
```

Finally, to perform the actual copy to the SD card, run: 

```bash
$ sudo bmaptool copy --bmap image.wic.bmap image.wic.bz2 /dev/sdb
bmaptool: info: block map format version 2.0
bmaptool: info: 93287 blocks of size 4096 (364.4 MiB), mapped 50084 blocks (195.6 MiB or 53.7%)
bmaptool: info: copying image 'image.wic.bz2' to block device '/dev/sdb' using bmap file 'image.wic.bmap'
bmaptool: WARNING: failed to enable I/O optimization, expect suboptimal speed (reason: cannot switch to the 'noop' I/O scheduler: [Errno 22] Invalid argument)
bmaptool: info: 100% copied
bmaptool: info: synchronizing '/dev/sdb'
bmaptool: info: copying time: 11.9s, copying speed 16.4 MiB/sec
```
## Working with Other Boards

This Yocto layer was also tested with a PYNQ-Z1 board. No layer modification was necessary. The minor changes required were the usual changes for setting up a petalinux project. The following steps were taken:
- Create a Vivado block design with just the Zynq processor block. Then, export the Vivado design, creating the `xsa` file;
- Run petalinux `settings.sh` script;
- Run the following petalinux commands:

```
$ petalinux-create -t project --template zynq -n <proj-name>
$ petalinux-config --get-hw-description=<the-generated.xsa>
...
```
- The rest of the flow is exactly the same as for the ZCU102 board;
- Burn the SD card, configure the board for SD boot, plug the serial interface to have the terminal for testing the image.

## Suggested Git Branch Organization

For future reference, it is suggested the following git branch organization for the support of future petalinux or yocto-based design flows. 

```
main
├── petalinux
│   ├── v2020.2
│   │   ├── feature_1
│   │   └── feature_2
│   ├── v2021.1
│   └── ...
└── yocto
    ├── dunfell
    ├── zeus
    └── ...
```

## TODO

 - [x] Supporting PREEMPT_RT;
 - [x] Support for wic image format and [bmaptool](https://github.com/intel/bmap-tools); 
 - [x] Include a working device-tree.bbappend in the layer;
   - [x] https://github.com/analogdevicesinc/meta-adi/blob/master/meta-adi-xilinx/recipes-bsp/fpga-manager-util/fpga-manager-util_%25.bbappend
   - [x] https://github.com/analogdevicesinc/meta-adi/blob/master/meta-adi-xilinx/recipes-bsp/device-tree/files/pl-zynqmp-zcu102-rev10-ad9361-fmcomms2-3-overlay.dtsi
   - [x] https://xilinx-wiki.atlassian.net/wiki/spaces/A/pages/61669922/Customizing+Device+Trees+in+Xilinx+Yocto
   - [x] Check support for device tree fragments;
   [ ] follow the [recipe style guidelines](https://www.openembedded.org/wiki/Styleguide);
 - [ ] Testing:
   - [ ] Implement [testing](https://docs.yoctoproject.org/test-manual/intro.html#) and integrate with a [buildbot CI framework](https://git.yoctoproject.org/yocto-autobuilder2/tree/README.md); 
   - [ ] [Testing Continuously Applications Using a Cloud Based Infrastructure Using Virtualization and Real Hardware in the Loop](https://iot.bzh/en/publications/44-2021/121-testing-continuously-applications-using-a-cloud-based-infrastructure-using-virtualization-and-real-hardware-in-the-loop);
 - [ ] Realtime validation
   - [ ] [Real-time testing with Fuego](https://elinux.org/images/4/43/ELC2018_Real-time_testing_with_Fuego-181024m.pdf);
   - [ ] [realtime validation](https://github.com/toradex/rt-validation);
   - [ ] Yocto - [Tracing and Profiling](https://wiki.yoctoproject.org/wiki/Tracing_and_Profiling);
 - [ ] Remote debugging: 
   - [ ] [Cross debugging on Linux : A history, current state of the art and coming improvements](https://iot.bzh/en/publications/119-cross-debugging-on-linux-a-history,-current-state-of-the-art-and-coming-improvements);
   - [ ] [Yocto: Debugging With the GNU Project Debugger (GDB) Remotely](https://docs.yoctoproject.org/singleindex.html#debugging-with-the-gnu-project-debugger-gdb-remotely)
   - [ ] [Cross architecture remote debugging using gdb with Visual Studio Code (vscode) on Linux](https://medium.com/@karel.l.vermeiren/cross-architecture-remote-debugging-using-gdb-with-visual-studio-code-vscode-on-linux-c0572794b4ef)
  - [ ] [How to set up Kernel debug using PetaLinux](https://support.xilinx.com/s/article/66853?language=en_US);
 - [ ] Apply more realtime recommendations, such as:
   - [ ] [Practical Real-Time Linux](https://elinux.org/images/d/d7/Practical-Real-Time-Linux-ELCE15.pdf)
   - [ ] Move towards similar goals to [AGL Real-time Architecture Options for Critical Services](https://iot.bzh/download/public/2018/ALS-Tokyo/Fulup/agl-realtime-options-fulup-tokyo-2018.pdf)
   - [ ] [Understanding Linux real-time with PREEMPT_RT](https://bootlin.com/doc/training/preempt-rt/preempt-rt-slides.pdf)
 - [ ] RTOS and isolation
   - [ ] [Hardware Isolation Running RTOS Concurrently with AGL on Renesas R-Car](https://iot.bzh/download/public/2021/agl-amm/March17_JulienMassot_Hardware-isolation-running-rtos-concurrently-with-agl-on-renesas-r-car.pdf);
   - [ ] [meta-zephyr](https://wiki.yoctoproject.org/wiki/TipsAndTricks/BuildingZephyrImages);
   - [ ] [zephyr docker](https://github.com/iotbzh/ZephyrDocker/blob/master/Dockerfile)
   - [ ] [meta-jailhouse](https://github.com/Limoto/meta-jailhouse)
 - [ ] [Build Ubuntu Image](https://github.com/ikwzm/ZynqMP-FPGA-Ubuntu20.04-Ultra96) or from [this tutorial](https://highlevel-synthesis.com/2019/12/15/running-ubuntu-on-ultra96v2-using-petalinux-2019-2-with-networking-and-linux-header/);

## References

 - [A practical guide to BitBake](https://a4z.gitlab.io/docs/BitBake/guide.html)
 - [bitbake commands](https://backstreetcoder.com/bitbake-commands/)
 - [Wind River Linux - Platform Development Guide](https://docs.windriver.com/bundle/Wind_River_Linux_Platform_Developers_Guide_LTS_19/) excelent documentation on how to use Yocto for their Linux product;
 - [Realtime Testing Best Practices](https://elinux.org/Realtime_Testing_Best_Practices);
 - [Device Tree Reference](https://elinux.org/Device_Tree_Reference);
 - [Devicetree Overlay Notes](https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/plain/Documentation/devicetree/overlay-notes.rst);
 - [Introduction to Realtime Linux](https://www.youtube.com/watch?v=BKkX9WASfpI&t=1s).
 
## Contributions

  Did you find a bug in this layer ? Do you have some extensions or updates to add ? Please send us a Pull Request.

## Authors

 - Alexandre Amory (January 2022), ReTiS Lab, Scuola Sant'Anna, Pisa, Italy.

## Funding
 
This software package has been developed in the context of the [AMPERE project](https://ampere-euproject.eu/). This project has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 871669.
