package com.rxlogix.util

import groovy.util.logging.Slf4j
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.io.IOUtils

import java.util.zip.GZIPOutputStream

@Slf4j
public class FileUtil {


    public static void compressFile(File file, File output) throws IOException {
        ArrayList<File> list = new ArrayList<File>(1);
        list.add(file);
        compressFiles(list, output);
    }

    /**
     * Compress (tar.gz) the input files to the output file
     *
     * @param files The files to compress
     * @param output The resulting output file (should end in .tar.gz)
     * @throws IOException
     */
    public static void compressFiles(Collection<File> files, File output) throws IOException {
        log.debug ("Compressing " + files.size() + " to " + output.getAbsoluteFile());
        // Create the output stream for the output file
        FileOutputStream fos = new FileOutputStream(output);
        // Wrap the output file stream in streams that will tar and gzip everything
        TarArchiveOutputStream taos = new TarArchiveOutputStream(
                new GZIPOutputStream(new BufferedOutputStream(fos)));

        try {
            // TAR has an 8 gig file limit by default, this gets around that
            taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR); // to get past the 8 gig limit
            // TAR originally didn't support long file names, so enable the support for it
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            // Get to putting all the files in the compressed output file
            for (File f : files) {
                addFilesToCompression(taos, f, ".");
            }
        } finally {
            // Close everything up
            taos?.close();
            fos?.close();
        }
    }

    /**
     * Does the work of compression and going recursive for nested directories
     * <p/>
     *
     * Borrowed heavily from http://www.thoughtspark.org/node/53
     *
     * @param taos The archive
     * @param file The file to add to the archive
     * @param dir The directory that should serve as the parent directory in the archivew
     * @throws IOException
     */
    private static void addFilesToCompression(TarArchiveOutputStream taos, File file, String dir) throws IOException {
        // Create an entry for the file
        taos.putArchiveEntry(new TarArchiveEntry(file, dir + File.separator + file.getName()));
        if (file.isFile()) {
            // Add the file to the archive
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            try {
                IOUtils.copy(bis, taos);
                taos.closeArchiveEntry();
            } finally {
                bis?.close();
            }
        } else if (file.isDirectory()) {
            // close the archive entry
            taos.closeArchiveEntry();
            // go through all the files in the directory and using recursion, add them to the archive
            for (File childFile : file.listFiles()) {
                addFilesToCompression(taos, childFile, file.getName());
            }
        }
    }
}