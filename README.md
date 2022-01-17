# meta-retis

Yocto layer/image from [Real-Time Systems Laboratory (ReTiS Lab)](https://retis.santannapisa.it/), [Scuola Superiore Sant'Anna (SSSA)](https://www.santannapisa.it/), Pisa, Italy.

## Requirements

This documentation considers the following requirements:
 - PetaLinux v2020.2;
 - ZCU102 UltraScale+ board;

Despite of that, we *believe* that this Yocto Layer is general enough, with minor tweaks, to be used with other Petalinux versions and Xilinx boards.

## Creating a PetaLinux Project

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
$ cd build
$ ln -s <yocto-download-dir> downloads
$ ln -s <yocto-sstate-cache> sstate-cache
```

These last set of commands run petalinux-config and exit it. This process will create the build directory in the newly created petalinux project. In the build directory, it is recommemded to reuse Yocto downloads and sstates directories from previous petalinux projects. If these two directories do not exist, no problem. It will only take longer to download all necessary code.

## Including `meta-retis` Layer

The next step is to include this layer into the petalinux project by running:

```
$ cd <proj-dir>
$ mkdir components/ext_source
$ cd components/ext_source
$ git clone -b petalinux https://github.com/fred-framework/meta-retis.git
$ cd meta-retis
$ tree
.
├── conf
│   └── layer.conf
├── LICENSE
├── README.md
├── recipes-core
│   ├── images
│   │   ├── retis-dev-image.bb
│   │   ├── retis-image.bb
│   │   └── retis-kernel-dev-image.bb
│   ├── packagegroups
│   │   └── retis-packagegroup-testing.bb
└── recipes-kernel
    └── linux
        └── linux-xlnx
            ├── files
            │   ├── defconfig
            │   ├── ftrace.cfg
            │   └── rt-frag.cfg
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

### PREEMPT_RT Kernel Support

**To be done !**

### RootFS Customization

The images already add software to the RootFS. However, it is also possible to browse a *menuconfig style* tool to select addtional tools by running:

```
$ petalinux-config -c rootfs
```

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
$ petalinux-package --boot --fsbl zynqmp_fsbl.elf --fpga system.bit --pmufw pmufw.elf --atf bl31.elf --u-boot u-boot.elf
```
If the vivado design uses the **PL** part. Note that this last option includes the .bit into the `BOOT.BIN`. 

The list of installed packages can be found in the `images/linux/rootfs.manifest` file.

## Image Deploy

Copy the `BOOT.BIN`, `image.ub`, and `boot.scr` files to the **boot partition** of the SD card, formated with **FAT32**. Copy the `rootfs` file to the **rootfs partition** of the SD card, formated with **EXT4**. 

## TODO

 - [ ] Supporting PREEMPT_RT;
 - [ ] Support for wic image format and [bmaptool](https://github.com/intel/bmap-tools); 
 - [x] Add [`stress-ng`](http://cgit.openembedded.org/openembedded-core/plain/meta/recipes-extended/stress-ng/stress-ng_0.13.09.bb) recipe or get it from [github](https://github.com/ColinIanKing/stress-ng). https://www.yoctoproject.org/pipermail/yocto/2015-August/026123.html;
 - [ ] Implement [testing](https://docs.yoctoproject.org/test-manual/intro.html#) and integrate with a [buildbot CI framework](https://git.yoctoproject.org/yocto-autobuilder2/tree/README.md); 
 - [ ] [realtime validation](https://github.com/toradex/rt-validation);
 - [ ] https://support.xilinx.com/s/article/66853?language=en_US;
 - [ ] Yocto - [Tracing and Profiling](https://wiki.yoctoproject.org/wiki/Tracing_and_Profiling);
 - [ ] Check support for device tree fragments;

## References

 - [A practical guide to BitBake](https://a4z.gitlab.io/docs/BitBake/guide.html)
 - [bitbake commands](https://backstreetcoder.com/bitbake-commands/)
 - [Wind River Linux - Platform Development Guide](https://docs.windriver.com/bundle/Wind_River_Linux_Platform_Developers_Guide_LTS_19/) excelent documentation on how to use Yocto for their Linux product;
 - [HOWTO setup Linux with PREEMPT_RT properly](https://wiki.linuxfoundation.org/realtime/documentation/howto/applications/preemptrt_setup);
 - [Realtime Testing Best Practices](https://elinux.org/Realtime_Testing_Best_Practices);
 - [Device Tree Reference](https://elinux.org/Device_Tree_Reference);
 - [Devicetree Overlay Notes](https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/plain/Documentation/devicetree/overlay-notes.rst);

## Contributions

  Did you find a bug in this layer ? Do you have some extensions or updates to add ? Please send us a Pull Request.

## Authors

 - Alexandre Amory (January 2022), ReTiS Lab, Scuola Sant'Anna, Pisa, Italy.

## Funding
 
This software package has been developed in the context of the [AMPERE project](https://ampere-euproject.eu/). This project has received funding from the European Union’s Horizon 2020 research and innovation programme under grant agreement No 871669.
