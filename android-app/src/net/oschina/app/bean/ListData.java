package net.oschina.app.bean;

import java.util.ArrayList;
import java.util.List;
import net.oschina.app.bean.SoftwareList.Software;

public class ListData {
	private List<News> newsData = new ArrayList<News>();
	private List<Software> softwareData = new ArrayList<Software>();
	private List<Blog> blogData = new ArrayList<Blog>();
	private List<Post> questionData = new ArrayList<Post>();
	private List<Tweet> tweetData = new ArrayList<Tweet>();
	private List<Active> activeData = new ArrayList<Active>();
	private List<Messages> msgData = new ArrayList<Messages>();
	private List<SearchList.Result> searchData = new ArrayList<SearchList.Result>();
	
	private int newsSumData;
	private int softwareSumData;
	private int blogSumData;
	private int questionSumData;
	private int tweetSumData;
	private int activeSumData;
	private int msgSumData;
	private int searchSumData;
	
	public ListData(){
		
	}

	public List<Software> getSoftwareData() {
		return softwareData;
	}

	public int getSoftwareDataSize(){
		return softwareData.size();
	}

	public void addSoftwareData(Software software){
		this.softwareData.add(software);
	}

	public void addAllSoftwareData(List<Software> softwareData){
		this.softwareData.addAll(softwareData);
	}

	public void setSoftwareData(List<Software> softwareData) {
		this.softwareData = softwareData;
	}

	public int getSoftwareSumData() {
		return softwareSumData;
	}

	public void setSoftwareSumData(int softwareSumData) {
		this.softwareSumData = softwareSumData;
	}

	public void addSoftwareSumData(int softwareSumData){
		this.softwareSumData += softwareSumData;
	}

	public int getNewsSumData() {
		return newsSumData;
	}

	public void setNewsSumData(int NewsSumData) {
		this.newsSumData = NewsSumData;
	}
	
	public void addNewsSumData(int NewsSumData){
		this.newsSumData += NewsSumData;
	}

	public int getBlogSumData() {
		return blogSumData;
	}

	public void setBlogSumData(int BlogSumData) {
		this.blogSumData = BlogSumData;
	}
	
	public void addBlogSumData(int BlogSumData){
		this.blogSumData += BlogSumData;
	}

	public int getQuestionSumData() {
		return questionSumData;
	}

	public void setQuestionSumData(int QuestionSumData) {
		this.questionSumData = QuestionSumData;
	}
	
	public void addQuestionSumData(int QuestionSumData){
		this.questionSumData += QuestionSumData;
	}

	public int getTweetSumData() {
		return tweetSumData;
	}

	public void setTweetSumData(int TweetSumData) {
		this.tweetSumData = TweetSumData;
	}
	
	public void addTweetSumData(int TweetSumData){
		this.tweetSumData += TweetSumData;
	}

	public int getActiveSumData() {
		return activeSumData;
	}

	public void setActiveSumData(int ActiveSumData) {
		this.activeSumData = ActiveSumData;
	}
	
	public void addActiveSumData(int ActiveSumData){
		this.activeSumData += ActiveSumData;
	}

	public int getMsgSumData() {
		return msgSumData;
	}

	public void setMsgSumData(int MsgSumData) {
		this.msgSumData = MsgSumData;
	}
	
	public void addMsgSumData(int MsgSumData){
		this.msgSumData += MsgSumData;
	}

	public List<News> getNewsData() {
		return newsData;
	}
	
	public int getNewsDataSize(){
		return newsData.size();
	}

	public void setNewsData(List<News> NewsData) {
		this.newsData = NewsData;
	}
	
	public void addNewsData(News news){
		this.newsData.add(news);
	}
	
	public void addAllNewsData(List<News> NewsData){
		this.newsData.addAll(NewsData);
	}

	public List<Blog> getBlogData() {
		return blogData;
	}
	
	public int getBlogDataSize(){
		return blogData.size();
	}

	public void setBlogData(List<Blog> BlogData) {
		this.blogData = BlogData;
	}
	
	public void addBlogData(Blog blog){
		this.blogData.add(blog);
	}
	
	public void addAllBlogData(List<Blog> BlogData){
		this.blogData.addAll(BlogData);
	}

	public List<Post> getQuestionData() {
		return questionData;
	}
	
	public int getQuestionDataSize(){
		return questionData.size();
	}

	public void setQuestionData(List<Post> QuestionData) {
		this.questionData = QuestionData;
	}
	
	public void addQuestionData(Post post){
		this.questionData.add(post);
	}
	
	public void addAllQuestionData(List<Post> QuestionData){
		this.questionData.addAll(QuestionData);
	}

	public List<Tweet> getTweetData() {
		return tweetData;
	}
	
	public int getTweetDataSize(){
		return tweetData.size();
	}

	public void setTweetData(List<Tweet> TweetData) {
		this.tweetData = TweetData;
	}
	
	public void addTweetData(Tweet tweet){
		this.tweetData.add(tweet);
	}
	
	public void addAllTweetData(List<Tweet> TweetData){
		this.tweetData.addAll(TweetData);
	}

	public List<Active> getActiveData() {
		return activeData;
	}
	
	public int getActiveDataSize(){
		return activeData.size();
	}

	public void setActiveData(List<Active> ActiveData) {
		this.activeData = ActiveData;
	}
	
	public void addActiveData(Active active){
		this.activeData.add(active);
	}
	
	public void addAllActiveData(List<Active> ActiveData){
		this.activeData.addAll(ActiveData);
	}

	public List<Messages> getMsgData() {
		return msgData;
	}
	
	public int getMsgDataSize(){
		return msgData.size();
	}

	public void setMsgData(List<Messages> MsgData) {
		this.msgData = MsgData;
	}
	
	public void addMsgData(Messages messages){
		this.msgData.add(messages);
	}
	
	public void addAllMsgData(List<Messages> MsgData){
		this.msgData.addAll(MsgData);
	}

	public List<SearchList.Result> getSearchData() {
		return searchData;
	}
	
	public int getSearchDataSize() {
		return searchData.size();
	}

	public void setSearchData(List<SearchList.Result> SearchData) {
		this.searchData = SearchData;
	}
	
	public void addSearchData(SearchList.Result result) {
		this.searchData.add(result);
	}
	
	public void addAllSearchData(List<SearchList.Result> SearchData) {
		this.searchData.addAll(SearchData);
	}

	public int getSearchSumData() {
		return searchSumData;
	}

	public void setSearchSumData(int SearchSumData) {
		this.searchSumData = SearchSumData;
	}
	
	public void addSearchSumData(int SearchSumData) {
		this.searchSumData += SearchSumData;
	}
}
