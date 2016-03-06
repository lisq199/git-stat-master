package io.ologn.gitstat.vis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import io.ologn.common.color.ColorCategory;
import io.ologn.gitstat.jgit.RevCommitUtils;
import io.ologn.gitstat.stat.FileAge;
import io.ologn.gitstat.stat.FileAgeOverCommits;
import io.ologn.gitstat.stat.FileAuthorship;
import io.ologn.gitstat.stat.FileCommitStat;
import io.ologn.gitstat.stat.LineAuthorship;
import io.ologn.gitstat.stat.LineAuthorshipOverCommits;
import io.ologn.gitstat.tokenizer.TokenParser;
import io.ologn.gitstat.vis.chartjs.ChartJsPie;

public class VisRunner {
	
	protected String dotGitPath;
	
	protected VisRunner() {}
	
	protected VisRunner(String dotGitPath) {
		this();
		this.dotGitPath = dotGitPath;
	}
	
	public void type1(String filePath, String revstr, TokenParser parser,
			boolean combineSmallValues, double percentageForOther) {
		try (
			Git git = Git.open(new File(dotGitPath));
			Repository repo = git.getRepository();
		) {
			FileCommitStat stat = FileCommitStat.calculate(git, repo,
					revstr, filePath, parser.parseToken());
			FileAuthorship fileAuthorship = FileAuthorship.calculate(
					parser.getTokenValue(), stat);
			Map<String, Double> chartJsPieMap;
			if (combineSmallValues) {
				chartJsPieMap = fileAuthorship.getTrimmedChartJsPieMap(
						percentageForOther);
			} else {
				chartJsPieMap = fileAuthorship.getChartJsPieMap();
			}
			String htmlString = ChartJsPie.init().parseMap(chartJsPieMap)
					.createHtmlString();
			BrowserLauncher.launchWithHtmlText(htmlString);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RevisionSyntaxException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
	
	public void type2(String filePath, boolean sortByAge,
			boolean sortByAgeAscending,
			int pixelHeight, int pixelWidth, boolean displayVertical) {
		try (
			Git git = Git.open(new File(dotGitPath));
			Repository repo = git.getRepository();
		) {
			Iterable<RevCommit> commits = RevCommitUtils.getCommitsWithPath(
					git, filePath);
			List<FileAge> fileAges = FileAge.calculateMultiple(
					git, repo, commits, filePath);
			FileAgeOverCommits faoc = FileAgeOverCommits.calculate(
					repo, fileAges, filePath, true);
			List<long[]> colorPixelsDataArrays;
			if (sortByAge) {
				colorPixelsDataArrays = faoc
						.getColorPixelsDataArraysSortedByAge(
								sortByAgeAscending);
			} else {
				colorPixelsDataArrays = faoc.getColorPixelsDataArrays();
			}
			String htmlString = ColorPixels.init()
					.setPixelHeight(pixelHeight)
					.setPixelWidth(pixelWidth)
					.setColorCategory(ColorCategory.ORANGERED_TO_GREEN
							.reverse())
					.parse(colorPixelsDataArrays, new ArrayList<String[]>(),
							displayVertical)
					.createHtmlString();
			BrowserLauncher.launchWithHtmlText(htmlString);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
	
	public void type3(String filePath, boolean sortByAuthor,
			int pixelHeight, int pixelWidth, boolean displayVertical) {
		try (
			Git git = Git.open(new File(dotGitPath));
			Repository repo = git.getRepository();
		) {
			Iterable<RevCommit> commits = RevCommitUtils.getCommitsWithPath(
					git, filePath);
			List<LineAuthorship> lineAuthorships = LineAuthorship
					.calculateMultiple(git, repo, commits, filePath);
			LineAuthorshipOverCommits laoc = LineAuthorshipOverCommits
					.calculate(repo, lineAuthorships, filePath, true);
			List<long[]> colorPixelsDataArrays;
			List<String[]> colorPixelsTitleArrays;
			if (sortByAuthor) {
				colorPixelsDataArrays = laoc
						.getColorPixelsDataArraysSortedByAuthor();
				colorPixelsTitleArrays = laoc
						.getColorPixelsTitleArraysSortedByAuthor();
			} else {
				colorPixelsDataArrays = laoc.getColorPixelsDataArrays();
				colorPixelsTitleArrays = laoc.getColorPixelsTitleArrays();
			}
			
			String htmlString = ColorPixels.init()
					.setPixelHeight(pixelHeight)
					.setPixelWidth(pixelWidth)
					.setColorCategory(ColorCategory.D3_CATEGORY20)
					.parse(colorPixelsDataArrays, colorPixelsTitleArrays,
							displayVertical)
					.createHtmlString();
			BrowserLauncher.launchWithHtmlText(htmlString);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
	
	public static VisRunner init(String dotGitPath) {
		return new VisRunner(dotGitPath);
	}

}
