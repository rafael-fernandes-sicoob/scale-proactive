Thanks for your interest in ProActive.

ProActive 2015-01-27

You can find the documentation of ProActive in the docs directory:

	* Documentation in PDF format

In order to start experimenting with ProActive:

	* Set JAVA_HOME environment variable to the directory where 1.6 or greater JDK is installed

	Run the examples by going in the examples directory and launching the suitable scripts for your platform.
	
	For instance:
          o Under Linux:
            export JAVA_HOME=<JDK_INSTALL_PATH> (Bash syntax)
            cd examples/nbody
	    ./nbody.sh
            If you get a "permission denied" when running scripts, check the permissions of the scripts and change them accordingly.
            chmod -R 755 .

          o Under Windows:
            set JAVA_HOME=<JDK_INSTALL_PATH>
            cd examples\nbody
            nbody.bat


	Check ProActive version and configuration:
	    java -jar dist/lib/ProActive.jar 	

If you want to recompile all sources and generate all jar files (might be useful with SVN version):

	o Under Linux:
	  cd compile
	  ./build deploy.all  (check that the build script has executable permission)

	o Under Windows:
	  cd compile
	  build.bat deploy.all


If you want only to compile all sources (and not the jar files):

	o Under Linux:
	  cd compile
	  ./build compile.all  (check that the build script has executable permission)

	o Under Windows:
	  cd compile
	  build.bat compile
	  
	  
If you encounter Security exceptions, you might need to set up a Java policy file.


Enjoy ProActive !
