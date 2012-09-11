/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; see the file COPYING.  If not, write to
 * the Free Software Foundation, 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

#import "RunFigTree.h"
#import <Foundation/Foundation.h>

CFDataRef runFigTree(CFURLRef URL)
{
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];    
    
    NSURL * nsURL = (NSURL *) URL;
    NSString * path = [nsURL path];
    NSPipe * output = [NSPipe pipe];
    NSPipe * error = [NSPipe pipe];
    NSTask * figtreeTask = [[NSTask alloc] init];
		
    NSBundle * myBundle = [NSBundle bundleWithIdentifier: @"figtree.QLFigTree"];
    NSString * jarFile = [myBundle pathForResource: @"figtree-pdf" ofType: @"jar"];
    NSArray * arguments = [NSArray arrayWithObjects:
                           @"-Xmx128M", 
						   @"-Djava.awt.headless=true",
                           @"-jar", jarFile,
                           path, nil];
    [figtreeTask setLaunchPath: @"/System/Library/Frameworks/JavaVM.framework/Home/bin/java"];
    [figtreeTask setArguments: arguments];
    [figtreeTask setStandardOutput: output];
    [figtreeTask setStandardError: error];
    
//	NSLog(@"Attempting to run FigTree");
	
    NSFileHandle * outputFile = [output fileHandleForReading];
    NSFileHandle * errorFile = [error fileHandleForReading];
    [figtreeTask launch];
    NSData * data = [[outputFile readDataToEndOfFile] retain];
    [errorFile readDataToEndOfFile];
    [figtreeTask waitUntilExit];
    [figtreeTask release];
    
//	NSString *aStr = [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
//
//	NSLog(aStr);

    [pool release];
	
    return (CFDataRef) data;
}
