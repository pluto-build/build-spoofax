package build.pluto.buildspoofax.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.IOFileFilter;

public class PatternFileFilter implements IOFileFilter {

	private List<Pattern> alternativePatterns;
	private boolean fullpath;
	
	public PatternFileFilter(String... regexes) {
		this(false, regexes);
	}
	public PatternFileFilter(boolean fullpath, String... regexes) {
		this.fullpath = fullpath;
		this.alternativePatterns = new ArrayList<>();
		for (String regex : regexes) 
			this.alternativePatterns.add(Pattern.compile(regex));
	}
	
	@Override
	public boolean accept(File file) {
		String path = fullpath ? file.getPath() : file.getName();
		for (Pattern pat : alternativePatterns)
			if (pat.matcher(path).matches())
				return true;
		return false;
	}

	@Override
	public boolean accept(File dir, String name) {
		String path = fullpath ? new File(dir, name).getPath() : name;
		for (Pattern pat : alternativePatterns)
			if (pat.matcher(path).matches())
				return true;
		return false;
	}

}
