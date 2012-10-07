                  FigTree v1.4 2006-2012
                        Andrew Rambaut

              Institute of Evolutionary Biology
                    University of Edinburgh
                      a.rambaut@ed.ac.uk


UNIX/Linux/Mac OS X (command-line) version README
Last updated: a.rambaut@ed.ac.uk - 8th October 2012

Contents:
1) INTRODUCTION
2) VERSION HISTORY
3) INSTALLATION
4) OPENING TREES
5) USING FIGTREE
6) EXPORTING RESULTS
7) SUPPORT & LINKS
8) ACKNOWLEDGMENTS

___________________________________________________________________________
1) INTRODUCTION

FigTree is designed as a graphical viewer of phylogenetic trees and as a program for producing publication-ready figures. As with most of my programs, it was written for my own needs so may not be as polished and feature-complete as a commercial program. On the other hand, I don't think there are any commercial tree drawing programs.

___________________________________________________________________________
2) VERSION HISTORY

v1.4 Released 8th October 2012.

New Features:
	Added a 'Node Shape' option which can draw circles, squares or diamonds on each node. These can be coloured and sized by attributes.
	
	Added a 'Legend' option which can produce a colour legend for any attribute/colour scheme being used.
	
	New colour scheme for discrete attributes based on a HSB colour wheel. This replaces the old fixed sequence of colours and is more customizable. Attribute values can be re-ordered.
	
	New colour scheme for continuous attributes also based on HSB. The old interpolating colour scheme is still available.
	
	Colour schemes are now selectable for particular attributes and these are saved and loaded in FigTree files.
	
	Numerous tweaks to the controls to make things more convenient. These include the reintroduction of font size spinners (in addition to font selection dialogs) and scrolling of the entire control panel when larger than the window.
	
	New zooming/expansion. The maximum extent of zooming is now proportional to the number of tips in the tree. I.e., the bigger the tree the more you can zoom in. Also added a non-linearity to zooming (starts slower).
		
Bug Fixes:
	Issue 52:	Significant figure spinners don't currently have an effect.
	Issue 51:	Preference fonts initially set to first font in list
	Issue 50:	Problem with MidPoint rooting a tree
	Issue 48:	Taxon labels not displaying traits of nodes
	Issue 45:	Nexus importer doesn't ignore unrecognised commands
	Issue 42:	Collapsed clades are clipped at the top of the page
	Issue 39:	Rerooting doesn't always work.
	
v1.3.1 Released 21st December 2009.

Bug Fixes:
	Re-introduced the graphic export formats that were inadvertently omitted in the previous version.

	Node bar panel now shows available attributes to display (bug introduced in previous version).

v1.3 Released 14th December 2009.

New Features:
	Added a 'Background' colouring option to the 'Appearance' panel so the colour under each clade can be controlled by an attribute.

	Added a 'Color by' option to tip, node and branch labels so the colour of the labels can be controlled by an attribute.

	Restored a Export to PDF option in the File menu. This uses a different library for creating PDFs from the Graphics export and doesn't require any settings (it should produce a PDF that matches the screen image).

	Implemented a 'QuickLook' plugin for Mac OS X.

Bug Fixes:
	Fixed an issue with 'branch' attributes not being loaded from a NEXUS file.

	The tree panel now has focus when the window opens which allows direct copy/pasting of tree files.

	New attributes are available in the Find panel.

v1.2.3 Released 10th Aug 2009.

New Features:
	Added a 'Reverse Axis' option to the Scale Axis settings - this reverses the direction of the scale axis.

	Added an option to turn on and off the grid lines in the Scale Axis settings.

Bug Fixes:
	The scale grid lines could be out of alignment with the axis.

	Annotation types being defined in the dialog box were not being created.

v1.2.2 Released 24th Feb 2009.

Bug Fixes:
	Exported NEWICK trees now have a terminal semi-colon.

	Text boxes in control panels are now editable again.

	Polar trees with 'Align Tip Labels' could cause the tree to go off the page.

v1.2.1 Released 9th Jan 2009.

New Features:
	Taxon, branch and node labels now have a "Font" button which can be used to set the font for these labels.

	Added a "Gradient" check box to the "Appearance" panel. This will try to produce gradual colour changes on the branches.

Bug Fixes:
	Fixed a problem reloading trees with integer annotations.

	'Preferences' wasn't available on Windows & Linux (it is now in the Edit menu).

	Now correctly exports trees 'As Displayed' (i.e., rooted or transformed).

v1.2 Released 30th Nov 2008.

New Features:
	Added a 'highlight' button which provides a block colouring for a clade.

	Add a continous colour gradient option to the 'Colour By' control in the Appearance panel. Use setup to activate (at present it disables the 'Width By' option.

	Added a 'Name' annotation that can be used to edit taxon names. It can also be used to name clades (and these are displayed for collapsed clades).

	Added a new drop down find panel that is activate by Find in the Edit menu or the find icon in the tool bar.

	Font sizes and a few other options can now be set in the 'Preferences' box. These act as defaults for new windows.

Bug Fixes:
	Fixed a problem with branch annotations being applied to the wrong branches when the tree is re-rooted.

	"Colour By" colours were reversed from those suggested in the Setup dialog.

	The "Colour By" setup didn't work as expected if Auto-Range was turned off.

	Made the control palette title bars slightly thinner.

v1.1.2 Released 6th Feb 2008.

New Features:
	Added new graphics export formats using the FreeHEP library. This includes SVG, EPS, PDF and EMF along with raster formats such as GIF, PNG etc.

	If a tree contains 'labels' (numbers such as bootstrap values before the branch length) then the user is given the opportunity to give these an informative name.

v1.1.1 Released 22nd January 2008.

New Features:
	Added the ability to format numerical node/branch labels as percentages (and Roman numerals).

	Added a 'setup' button for the 'Colour by' and 'Width by' controls in the 'Appearance' palette. These allow customization of the colours and line widths.

	Added hotkeys for menu options.

	Added direct menu options for midpoint rooting and node orderings (with hotkeys).

Bug Fixes:
	The filter (the search box on the toolbar) was not respecting the choice made in the popup menu.

	The search and filtering options were essentially non-functional. These now work as intended.

	FigTree crashed with an exception when run on certain Linux variants.

	FigTree crashed when displaying node/branch labels after the tree was manually re-rootd.

	Branch/node label choices were being lost when colouring or annotation tools were used.

	Removed the ugly icons from the menu options corresponding to toolbar buttons.

v1.1 Released 13th January 2008.

New Features:
	Added a collapse and a cartoon button - collapse produces a single taxon that represents the entire collapsed clade whereas cartoon creates a triangle that covers the same space as the clade.

	Reroot tree - select node and click reroot button in toolbar. Also select 'Midpoint root' in "Trees" control palette.

	Rotate nodes - select node and click rotate button in toolbar.

	Rescale trees - use "Time Scale" control palette (scale factor can be negative which reverses the time axis).

	Scale axis with grid lines - use the "Scale Axis" control palette.

	'FishEye' (1-dimensional) zooming - in "Layout" control palette. Hold down the 'Control' key ('Command/Apple' key on a Mac) to change the centre of view.

	Improved Find dialog - can find inequalities for numerical values.


Bug Fixes:
	Negative branch lengths are now correctly displayed rather than being transformed to unit branches.

	Many, other bug fixes and improvements.

v1.0 Released 30th October 2006.

	First Released Version

___________________________________________________________________________
3) INSTALLATION

FigTree requires a Java Virtual Machine to run. Many systems will already have this installed. It requires at least version 1.5 of Java to run. The latest versions of Java can be downloaded from:

<http://java.sun.com/>

If in doubt type "java -version" to see what version of java is installed (or if it is installed at all).

Mac OS X version 10.4 or better (Tiger) will already have a suitable version of Java installed. I am sorry but FigTree simply doesn't run on older versions of Mac OS X.

On Mac OS X and Windows, FigTree is run like any other application - double click on the application icon.

On UNIX/Linux systems, you may be able to double-click on the figtree.jar file or execute the "figtree" shell script.

___________________________________________________________________________
4) OPENING TREES

Simply select "Open" from the "File" menu or on Mac, drag a suitable file onto the icon in Finder or the Dock. You can also set files with a particular extension (say, ".tree") to open in FigTree as a default by selecting "Get Info", selecting FigTree as the "Open With" application and then pressing the "Change All" button.

Trees can be in NEXUS or NEWICK format.

___________________________________________________________________________
5) USING FIGTREE

At present there is no manual for using FigTree. I suggest you simply try playing with the various controls and buttons.

___________________________________________________________________________
6) EXPORTING RESULTS

To export the figure for publication or further editing in a graphics package, select "Export PDF" from the file menu to save as a PDF file.

Saving the tree from within FigTree will save in NEXUS format but add special comments and a command block which will enable FigTree to keep all the settings and annotations made to the tree. Other programs (such as PAUP) should ignore these settings. You can also 'Copy and Paste' the tree into any application that accepts text (the result will be a NEXUS format tree file).

___________________________________________________________________________
7) SUPPORT & LINKS

FigTree is a new program and is not complete or bug-free. Please email me to make suggestions or report bugs:

<a.rambaut@ed.ac.uk>

http://tree.bio.ed.ac.uk/

___________________________________________________________________________
8) ACKNOWLEDGMENTS

Thanks to the following for assisting with the creation or testing of FigTree:

	Alexei Drummond
	Joseph Heled
	Philippe Lemey
	Tulio de Oliveira
	Oliver Pybus
	Beth Shapiro
	Marc Suchard
	
