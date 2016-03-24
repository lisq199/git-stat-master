package io.ologn.gitstat.vis;

import java.io.File;
import java.io.IOException;
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

/**
 * For running visualizations.
 * @author lisq199
 */
public class VisRunner {
	
	/**
	 * The path to the ".git" file
	 */
	protected String dotGitPath;
	
	protected VisRunner() {}
	
	protected VisRunner(String dotGitPath) {
		this();
		this.dotGitPath = dotGitPath;
	}
	
	/**
	 * Visualization type 1: 
	 * Pie chart showing how much everyone owns a file at 
	 * a specified commit.
	 * @param filePath
	 * @param revstr
	 * @param parser
	 * @param combineSmallValues
	 * @param percentageForOther
	 */
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
	
	/**
	 * Visualization type 2: Color pixels showing the age of 
	 * each line of code over commits.
	 * @param filePath
	 * @param sortByAge
	 * @param sortByAgeAscending
	 * @param pixelHeight
	 * @param pixelWidth
	 * @param displayVertical
	 */
	public void type2(String filePath, boolean sortByAge,
			boolean sortByAgeAscending,
			int pixelHeight, int pixelWidth,
			boolean displayYear, boolean displayVertical) {
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
			Map<Long, String> colorPixelsTitleMap =
					faoc.getColorPixelsTitleMap();
			List<String> colorPixelsDatasetDescriptions = 
					faoc.getColorPixelsDatasetDescriptions(repo);
			Map<Integer, String> colorPixelsBookmarkMap = null;
			if (sortByAge) {
				colorPixelsDataArrays = faoc
						.getColorPixelsDataArraysSortedByAge(
								sortByAgeAscending);
			} else {
				colorPixelsDataArrays = faoc.getColorPixelsDataArrays();
			}
			if (displayYear) {
				colorPixelsBookmarkMap = faoc.getColorPixelsBookmarkMap(repo);
			}
			
			String htmlString = ColorPixels.init()
					.setPixelHeight(pixelHeight)
					.setPixelWidth(pixelWidth)
					.setColorCategory(ColorCategory.ORANGERED_TO_GREEN
							.reverse())
					.parse(colorPixelsDataArrays, colorPixelsTitleMap,
							colorPixelsDatasetDescriptions,
							colorPixelsBookmarkMap,
							displayVertical, true)
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
	
	/**
	 * Visualization type 3: Color pixels showing the authorship 
	 * of each line of code over commits. 
	 * @param filePath
	 * @param sortByAuthor
	 * @param sortByAuthorContribution
	 * @param pixelHeight
	 * @param pixelWidth
	 * @param displayVertical
	 */
	public void type3(String filePath, boolean sortByAuthor,
			boolean sortByAuthorContribution,
			int pixelHeight, int pixelWidth, boolean displayYear,
			boolean displayVertical) {
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
			Map<Long, String> colorPixelsTitleMap =
					laoc.getColorPixelsTitleMap();
			List<String> colorPixelsDatasetDescriptions =
					laoc.getColorPixelsDatasetDescriptions(repo);
			Map<Integer, String> colorPixelsBookmarkMap = null;
			if (sortByAuthor) {
				if (sortByAuthorContribution) {
					colorPixelsDataArrays = laoc
							.getColorPixelsDataArraysSortedByContribution(
									false);
				} else {
					colorPixelsDataArrays = laoc
							.getColorPixelsDataArraysSortedByAuthorId();
				}
			} else {
				colorPixelsDataArrays = laoc.getColorPixelsDataArrays();
			}
			if (displayYear) {
				colorPixelsBookmarkMap = laoc.getColorPixelsBookmarkMap(repo);
			}
			
			String htmlString = ColorPixels.init()
					.setPixelHeight(pixelHeight)
					.setPixelWidth(pixelWidth)
					.setColorCategory(ColorCategory.D3_CATEGORY20)
					.parse(colorPixelsDataArrays, colorPixelsTitleMap,
							colorPixelsDatasetDescriptions,
							colorPixelsBookmarkMap,
							displayVertical, false)
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
	
	/**
	 * Initialize
	 * @param dotGitPath
	 * @return
	 */
	public static VisRunner init(String dotGitPath) {
		return new VisRunner(dotGitPath);
	}

}
