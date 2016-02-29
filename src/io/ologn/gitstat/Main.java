package io.ologn.gitstat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import io.ologn.common.color.ColorCategory;
import io.ologn.gitstat.jgit.MiscJGitUtils;
import io.ologn.gitstat.jgit.RevCommitUtils;
import io.ologn.gitstat.stat.FileAge;
import io.ologn.gitstat.stat.FileAgeOverCommits;
import io.ologn.gitstat.stat.FileAuthorship;
import io.ologn.gitstat.stat.FileCommitStat;
import io.ologn.gitstat.tokenizer.BetterParserC;
import io.ologn.gitstat.tokenizer.TokenParser;
import io.ologn.gitstat.vis.BrowserLauncher;
import io.ologn.gitstat.vis.ColorPixels;

public class Main {

	public static void main(String[] args) {
		
		final String path = "abspath.c";
		
		try (
			Git git = MiscJGitUtils.getGit(new File(
					SystemUtils.USER_HOME + File.separator
					+ "/Developer/GitHub/git-test/.git"));
			Repository repo = git.getRepository()
		) {
			Iterable<RevCommit> commits = RevCommitUtils.getCommitsWithPath(git, path);
			
			for (RevCommit commit : commits) {
                System.out.println(commit);
            }
			
			ObjectId id = repo.resolve("HEAD");
			
			TokenParser parser = new BetterParserC();
			
			FileCommitStat stat = FileCommitStat.calculate(git, id, path, parser.parseToken());
			System.out.println(stat);
			
			FileAuthorship fAuthorship = FileAuthorship.calculate(parser.getTokenValue(), stat);
			
			System.out.println();
			System.out.println(fAuthorship);
			System.out.println();
			
			/*commits = RevCommitUtils.getCommitsWithPath(git, path);
			
			List<FileCommitStat> stats = FileCommitStat.calculateMultiple(git, repo, commits, path, parser.parseToken());
			List<FileAuthorship> fAuthorships = FileAuthorship.calculateMultiple(parser.getTokenValue(), stats);
			
			GitAuthor author = new GitAuthor("Michael Haggerty", "mhagger@alum.mit.edu");
			
			PersonAuthorship pAuthorship = PersonAuthorship.calculate(repo, author, fAuthorships);
			System.out.println(pAuthorship);*/
			
			/*String htmlString = ChartJsPie.init().parseMap(new FileAuthorshipVis(fAuthorship)
					.getTrimmedChartJsPieMap(3)).createHtmlString();
			BrowserLauncher.launchWithHtmlText(htmlString);*/
			
			commits = RevCommitUtils.getCommitsWithPath(git, path);
			List<FileAge> ages = FileAge.calculateMultiple(git, repo, commits, path);
			FileAgeOverCommits faoc = FileAgeOverCommits.calculate(repo, ages, path, true);
			Map<String, long[]> colorPixelMap = faoc.getColorPixelsMapSortedByAge(false);
			/*List<LineAuthorship> as = LineAuthorship.calculateMultiple(git, repo, commits, path);
			LineAuthorshipOverCommits laoc = LineAuthorshipOverCommits.calculate(repo, as, path, true);
			Map<String, long[]> colorPixelMap = laoc.getColorPixelMap();*/
			String htmlString = ColorPixels.init()
					.setPixelHeight(1).setPixelWidth(10)
					.setColorCategory(ColorCategory.ORANGERED_TO_GREEN.reverse())
					.parseMap(colorPixelMap, false)
					.createHtmlString();
			BrowserLauncher.launchWithHtmlText(htmlString);
			
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RevisionSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AmbiguousObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
