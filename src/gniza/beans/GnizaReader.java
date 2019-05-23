package gniza.beans;

import java.io.IOException;
import java.util.List;

public interface GnizaReader
{
    List<FileGniza> readFiles() throws IOException;
}
