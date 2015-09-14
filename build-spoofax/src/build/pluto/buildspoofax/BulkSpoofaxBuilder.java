package build.pluto.buildspoofax;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.file.FileAccess;
import org.metaborg.util.file.FileUtils;

import build.pluto.builder.bulk.BulkBuilder;
import build.pluto.output.Output;
import build.pluto.stamp.FileHashStamper;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

abstract public class BulkSpoofaxBuilder<In extends SpoofaxInput, Out extends Output, SubIn extends SpoofaxInput>
    extends BulkBuilder<In, Out, SubIn> {

    protected final SpoofaxContext context;

    public BulkSpoofaxBuilder(In input) {
        super(input);
        this.context = input.context;
    }

    @Override protected Stamper defaultStamper() {
        return SpoofaxContext.BETTER_STAMPERS ? FileHashStamper.instance : LastModifiedStamper.instance;
    }

    protected void processFileAccess(FileAccess access) {
        for(FileObject fileObject : access.reads()) {
            final File file = FileUtils.toFile(fileObject);
            require(file);
        }
        for(FileObject fileObject : access.writes()) {
            final File file = FileUtils.toFile(fileObject);
            provide(file);
        }
    }
}
