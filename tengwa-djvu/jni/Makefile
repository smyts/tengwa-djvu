#C- This program is free software; you can redistribute it and/or 
#C- modify it under the terms of the GNU General Public License, 
#C- either Version 2 of the License or (at your option) any later
#C- version.  The license should have accompanied the program 
#C- or you may obtain a copy of the license from the Free Software
#C- Foundation at http://www.fsf.org.
#C- 
#C- This program is distributed in the hope that it will be useful,
#C- but WITHOUT ANY WARRANTY; without even the implied warranty of
#C- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#C- GNU General Public License for more details.


SHELL = /bin/bash
INSTALL = /usr/bin/install -c
RM = /bin/rm -f 
LIBTOOL = $(SHELL) $(top_builddir)/libtool
LIBTOOL_DEPS = config/ltmain.sh

srcdir = .
prefix = /usr/local
exec_prefix = ${prefix}
bindir = ${exec_prefix}/bin
datarootdir = ${prefix}/share

datadir = ${datarootdir}
libdir = ${exec_prefix}/lib
mandir = ${datarootdir}/man

SUBDIRS_FIRST = libdjvu tools

SUBDIRS_XML = xmltools
#SUBDIRS_XML = 

#SUBDIRS_GUI = gui
SUBDIRS_GUI =

SUBDIRS_I18N = i18n
#SUBDIRS_I18N =

SUBDIRS_LAST = desktopfiles

SUBDIRS = ${SUBDIRS_FIRST} ${SUBDIRS_XML} \
          ${SUBDIRS_GUI} ${SUBDIRS_I18N} \
	  ${SUBDIRS_LAST}

world: all
	@echo "Congratulations: the compilation was successful!"

all depend clean: FORCE
	for d in ${SUBDIRS} ; \
	  do ( cd $$d && ${MAKE} $@ ) || exit ; done

distclean: FORCE
	for d in ${SUBDIRS}; do ( cd $$d && ${MAKE} $@ ); done
	-find . -name Makefile -exec ${RM} {} \;
	-${RM} config.h config.log config.status
	-${RM} libtool

# Modularized install

install: install-libdjvu

install: install-utils

install-utils: install-tools

install-utils: install-xmltools

#install: install-gui

install: install-i18n

install: install-desktopfiles

install-libdjvu: FORCE
	( cd libdjvu && ${MAKE} install )

install-tools: FORCE
	( cd tools && ${MAKE} install )

install-xmltools: FORCE
	( cd xmltools && ${MAKE} install )

install-gui: FORCE
	( cd gui && ${MAKE} install )

install-i18n: FORCE
	( cd i18n && ${MAKE} install )

install-desktopfiles: FORCE
	( cd desktopfiles && ${MAKE} install )

libtool: ${LIBTOOL_DEPS} configure.ac
	${SHELL} ./config.status --recheck

FORCE:
.PHONY: FORCE
