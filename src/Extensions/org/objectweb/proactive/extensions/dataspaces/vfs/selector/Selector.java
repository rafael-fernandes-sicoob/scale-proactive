/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.dataspaces.vfs.selector;

import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/** An helper class to select a set of files.
 * 
 * Since {@link DataSpacesFileObject#findFiles(org.objectweb.proactive.extensions.dataspaces.api.FileSelector)}
 * cannot only takes a predefined selector as parameter, this class allow to pass a custom
 * selector 
 */
public class Selector {

    /**
     * Traverses the descendants of this file, and builds a list of selected
     * files.
     */
    static public void findFiles(final DataSpacesFileObject fo, final FileSelector selector,
            final boolean depthwise, final List<DataSpacesFileObject> selected) throws FileSystemException {
        try {
            if (fo.exists()) {
                // Traverse starting at this file
                final FileSelectInfo info = new FileSelectInfo();
                info.setBaseFolder(fo);
                info.setDepth(0);
                info.setFile(fo);
                traverse(info, selector, depthwise, selected);
            }
        } catch (final Exception e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Traverses a file.
     */
    private static void traverse(final FileSelectInfo fileInfo, final FileSelector selector,
            final boolean depthwise, final List<DataSpacesFileObject> selected) throws Exception {
        // Check the file itself
        final DataSpacesFileObject file = fileInfo.getFile();
        final int index = selected.size();

        // If the file is a folder, traverse it
        if (file.getType().hasChildren() && selector.traverseDescendents(fileInfo) && file.isReadable()) {
            final int curDepth = fileInfo.getDepth();
            fileInfo.setDepth(curDepth + 1);

            // Traverse the children
            final List<DataSpacesFileObject> children = file.getChildren();
            for (int i = 0; i < children.size(); i++) {
                final DataSpacesFileObject child = children.get(i);
                fileInfo.setFile(child);
                traverse(fileInfo, selector, depthwise, selected);
            }

            fileInfo.setFile(file);
            fileInfo.setDepth(curDepth);
        }

        // Add the file if doing depthwise traversal
        if (selector.includeFile(fileInfo)) {
            if (depthwise) {
                // Add this file after its descendents
                selected.add(file);
            } else {
                // Add this file before its descendents
                selected.add(index, file);
            }
        }
    }
}
