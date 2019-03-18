/*
 * Copyright (c) 2009, Harald Kuhr
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.twelvemonkeys.imageio.metadata.xmp;

import com.twelvemonkeys.imageio.metadata.AbstractCompoundDirectory;
import com.twelvemonkeys.imageio.metadata.Directory;

import java.util.Collection;

/**
* XMPDirectory
*
* @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
* @author last modified by $Author: haraldk$
* @version $Id: XMPDirectory.java,v 1.0 Nov 17, 2009 9:38:58 PM haraldk Exp$
*/
final class XMPDirectory extends AbstractCompoundDirectory {
    // TODO: Allow lookup of directories by namespace?
    // TODO: Allow merge/sync/comparison with IPTC/EXIF/TIFF metadata
    // TODO: Store size of root directory, to allow easy serializing (see isReadOnly comment)
    // TODO: XMPDirectory, maybe not even an AbstractDirectory
    //       - Keeping the Document would allow for easier serialization
    // TODO: Or use direct SAX parsing
    private final String toolkit;

    public XMPDirectory(Collection<? extends Directory> entries, String toolkit) {
        super(entries);

        this.toolkit = toolkit;
    }

    // TODO: Expose x:xmptk (getXMPToolkit(): String)
    /*public*/ String getWriterToolkit() {
        return toolkit;
    }

    @Override
    public boolean isReadOnly() {
        // TODO: Depend on <?xpacket end="w"?>/<?xpacket end="r"?> for writable/read-only respectively?
        // Spec says allow writing (even if "r"), if the container format is understood (ie. single file, known format, update checksums etc)
        return super.isReadOnly();
    }
}
