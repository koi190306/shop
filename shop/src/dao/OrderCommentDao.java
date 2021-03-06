package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import vo.*;
import commons.*;

public class OrderCommentDao {
	// [회원] 전자책 구입 후기 중복입력 방지
	// order_comment 테이블에 한 행의 order_no과 ebook_no가 매개변수의 값과 같다면 중복
	// 이미 작성된 후기 = true로 return
	public boolean insertOrderCommentCheck(int orderNo, int ebookNo) throws ClassNotFoundException, SQLException {
		boolean result = false;
		
		// 매개변수 값을 디버깅
		System.out.println(orderNo + "<--- OrderCommentDao.insertOrderCommentCheck parem : orderNo");
		System.out.println(ebookNo + "<--- OrderCommentDao.insertOrderCommentCheck parem : ebookNo");
		
		// DB 실행
		DBUtil dbUtil = new DBUtil();
		// dbUtil의 getConnection메서드를 사용하여 DB 연결
		Connection conn = dbUtil.getConnection();
		System.out.println(conn + "<--- conn");
		String sql = "SELECT order_no, ebook_no FROM order_comment WHERE order_no=? && ebook_no=?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, orderNo);
		stmt.setInt(2, ebookNo);
		// 디버깅 코드 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println(stmt + "<--- stmt");
		
		// SELECT 실행 값을 rs에 저장
		ResultSet rs = stmt.executeQuery();
		
		if(rs.next()) {
			result = true;
		}
				
		// 종료
		rs.close();
		stmt.close();
		conn.close();
		
		// 중복 : result = true, 중복X(후기작성가능) : false
		return result;
	}
	
	// [회원] 후기를 입력(추가) 하는 메서드
	// OrderComment 객체로 입력받아온 값을 DB에 insert 함
	public boolean insertOrderComment(OrderComment comment) throws ClassNotFoundException, SQLException {
		boolean result = false;
		
		// 매개변수 값을 디버깅
		System.out.println(comment + "<--- OrderCommentDao.insertOrderComment parem : comment");
		
		// DB 실행
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		// dbUtil의 getConnection메서드를 사용하여 DB 연결
		Connection conn = dbUtil.getConnection();
		System.out.println(conn + "<--- conn");
		String sql = "INSERT INTO order_comment(order_no, ebook_no, order_score, order_comment_content, create_date, update_date) VALUES (?,?,?,?,NOW(),NOW())";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, comment.getOrderNo());
		stmt.setInt(2, comment.getEbookNo());
		stmt.setInt(3, comment.getOrderScore());
		stmt.setString(4, comment.getOrderCommentContent());
		// 디버깅 코드 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println(stmt + "<--- stmt");
		
		// INSERT 실행
		int row = stmt.executeUpdate();
		if(row == 1) {
			result = true;
		}
		
		// 종료
		stmt.close();
		conn.close();
		
		// 성공 : result = true, 실패 : false
		return result;
	}
	
	// [사용자] 후기 별점의 평균을 구하는 메서드
	// 받아온 ebookNo를 기준으로 AVG를 사용하여 SELECT 함
	public double selectOrderScoreAvg(int ebookNo) throws ClassNotFoundException, SQLException{
		double avgScore = 0;
		
		// 매개변수 값을 디버깅
		System.out.println(ebookNo + "<--- OrderCommentDao.selectOrderScoreAvg parem : ebookNo");
		
		// DB 실행
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		// dbUtil의 getConnection메서드를 사용하여 DB 연결
		Connection conn = dbUtil.getConnection();
		System.out.println(conn + "<--- conn");
		String sql = "SELECT AVG(order_score) FROM order_comment WHERE ebook_no=? GROUP BY ebook_no;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, ebookNo);
		
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			avgScore = rs.getInt("AVG(order_score)");
		}
		
		// 종료
		stmt.close();
		conn.close();
		rs.close();
		
		return avgScore;
	}
	
	// [사용자] 후기를 SELECT 하는 메서드
	// 받아온 ebookNO를 기준으로 SELECT 하고 페이징함
	public ArrayList<OrderComment> selectCommentList(int beginRow, int rowPerPage, int ebookNo) throws ClassNotFoundException, SQLException{
		// list라는 리스트를 사용하기 위해 생성
		ArrayList<OrderComment> list = new ArrayList<OrderComment>();
		
		// 매개변수 값을 디버깅
		System.out.println(beginRow + "<--- OrderCommentDao.selectCommentList parem : beginRow");
		System.out.println(rowPerPage + "<--- OrderCommentDao.selectCommentList parem : rowPerPage");
		System.out.println(ebookNo + "<--- OrderCommentDao.selectCommentList parem : ebookNo");
		
		// DB 실행
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		Connection conn = dbUtil.getConnection();
		String sql = "SELECT order_score orderScore, order_comment_content orderCommentContent, create_date createDate FROM order_comment WHERE ebook_no=? ORDER BY create_date DESC LIMIT ?,?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, ebookNo);
		stmt.setInt(2, beginRow);
		stmt.setInt(3, rowPerPage);
		
		// 디버깅 코드 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println(stmt + "<--- stmt");
		
		// 데이터 가공 (자료구조화)
		// ResultSet이라는 특수한 타입에서 ArrayList라는 일반화된 타입으로 변환(가공)
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			// orderComment 객체 생성 후 저장
			OrderComment orderComment = new OrderComment();
			orderComment.setOrderScore (rs.getInt("orderScore"));
			orderComment.setOrderCommentContent (rs.getString("orderCommentContent"));
			orderComment.setCreateDate (rs.getString("createDate"));
			list.add(orderComment);
		}
		// 종료
		rs.close();
		stmt.close();
		conn.close();
				
		//list를 return
		return list;
	}
	
	// [사용자] 전자책 상세페이지의 후기를 페이징하기 위해 마지막 페이지를 구하는 메서드
	// totalCount(전체 행)의 값을 구해서 마지막 페이지의 값을 리턴해줌
	// ROW_PER_PAGE : 한 페이지에 보여줄 행의 값
	public int selectCommentListLastPage(int ROW_PER_PAGE, int ebookNo) throws ClassNotFoundException, SQLException{
		int totalCount = 0;
		int lastPage = 0;
		
		// 매개변수 값을 디버깅
		System.out.println(ROW_PER_PAGE + "<--- OrderCommentDao.selectCommentListLastPage parem : ROW_PER_PAGE");
		System.out.println(ebookNo + "<--- OrderCommentDao.selectCommentListLastPage parem : ebookNo");
		
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		Connection conn = dbUtil.getConnection();
		String sql = "SELECT count(*) FROM order_comment WHERE ebook_no = ?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, ebookNo);
		
		ResultSet rs = stmt.executeQuery();
		// 디버깅 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println("총 행의 개수 stmt : "+stmt);
		
		// totalCount 저장
		if(rs.next()) {
			totalCount = rs.getInt("count(*)");
		}
		System.out.println("totalCounnt(총 행의 개수) : "+totalCount);
				
		// 마지막 페이지
		// lastPage를 전체 행의 수와 한 페이지에 보여질 행의 수(rowPerPage)를 이용하여 구한다
		lastPage = totalCount / ROW_PER_PAGE;
		if(totalCount % ROW_PER_PAGE != 0) {
			lastPage+=1;
			}
		System.out.println("lastPage(마지막 페이지 번호) : "+lastPage);
				
		rs.close();
		stmt.close();
		conn.close();
				
		return lastPage;
	}
	
	// [사용자 & 관리자] 상품평 중에 최근 올라온 5개의 상품평을 SELECT하는 메서드
	// SELECT 한 값을 자료구조화 하여 list 생성 후 리턴
	public ArrayList<OrderComment> selectCreateOrderCommentList() throws ClassNotFoundException, SQLException {
		// list라는 리스트를 사용하기 위해 생성
		ArrayList<OrderComment> list = new ArrayList<OrderComment>();
		
		// DB 실행
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		Connection conn = dbUtil.getConnection();
		String sql = "SELECT order_no orderNo, ebook_no ebookNo, order_score orderScore, order_comment_content orderCommentContent, create_date createDate, update_date updateDate FROM order_comment ORDER BY create_date DESC LIMIT 0,5;";
		PreparedStatement stmt = conn.prepareStatement(sql);
		
		// 디버깅 코드 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println(stmt + "<--- stmt");
		
		// 데이터 가공 (자료구조화)
		// ResultSet이라는 특수한 타입에서 ArrayList라는 일반화된 타입으로 변환(가공)
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			// notice 객체 생성 후 저장
			OrderComment orderComment = new OrderComment();
			orderComment.setOrderNo(rs.getInt("orderNo"));
			orderComment.setEbookNo(rs.getInt("ebookNo"));
			orderComment.setOrderScore(rs.getInt("orderScore"));
			orderComment.setOrderCommentContent(rs.getString("orderCommentContent"));
			orderComment.setCreateDate(rs.getString("createDate"));
			orderComment.setUpdateDate(rs.getString("updateDate"));
			list.add(orderComment);
		}
		// 종료
		rs.close();
		stmt.close();
		conn.close();
				
		//list를 return
		return list;
	}
	
	// [관리자] 모든 상품평을 SELECT 하는 메서드
	// 관리자가 관리할 수 있게 입력된 모든 후기를 select 한다
	public ArrayList<OrderComment> selectCommentListByAdmin(int beginRow, int rowPerPage) throws ClassNotFoundException, SQLException{
		// list라는 리스트를 사용하기 위해 생성
		ArrayList<OrderComment> list = new ArrayList<OrderComment>();
		
		// 매개변수 값을 디버깅
		System.out.println(beginRow + "<--- OrderCommentDao.selectCommentListByAdmin parem : beginRow");
		System.out.println(rowPerPage + "<--- OrderCommentDao.selectCommentListByAdmin parem : rowPerPage");
		
		// DB 실행
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		Connection conn = dbUtil.getConnection();
		String sql = "SELECT order_no orderNo, ebook_no ebookNo, order_score orderScore, order_comment_content orderCommentContent, create_date createDate, update_date updateDate FROM order_comment ORDER BY create_date DESC LIMIT ?,?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, beginRow);
		stmt.setInt(2, rowPerPage);
		
		// 디버깅 코드 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println(stmt + "<--- stmt");
		
		// 데이터 가공 (자료구조화)
		// ResultSet이라는 특수한 타입에서 ArrayList라는 일반화된 타입으로 변환(가공)
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			// orderComment 객체 생성 후 저장
			OrderComment orderComment = new OrderComment();
			orderComment.setOrderNo(rs.getInt("orderNo"));
			orderComment.setEbookNo(rs.getInt("ebookNo"));
			orderComment.setOrderScore(rs.getInt("orderScore"));
			orderComment.setOrderCommentContent(rs.getString("orderCommentContent"));
			orderComment.setCreateDate(rs.getString("createDate"));
			orderComment.setUpdateDate(rs.getString("updateDate"));
			list.add(orderComment);
		}
		// 종료
		rs.close();
		stmt.close();
		conn.close();
				
		//list를 return
		return list;
	}
	
	// [관리자] 상품평 목록을 페이징하기 위해 마지막 페이지를 구하는 메서드
	// totalCount(전체 행)의 값을 구해서 마지막 페이지의 값을 리턴해줌
	// ROW_PER_PAGE : 한 페이지에 보여줄 행의 값
	public int selectCommentListLastPageByAdmin(int ROW_PER_PAGE) throws ClassNotFoundException, SQLException{
		int totalCount = 0;
		int lastPage = 0;
		
		// 매개변수 값을 디버깅
		System.out.println(ROW_PER_PAGE + "<--- OrderCommentDao.selectCommentListLastPageByAdmin parem : ROW_PER_PAGE");
		
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		Connection conn = dbUtil.getConnection();
		String sql = "SELECT count(*) FROM order_comment";
		PreparedStatement stmt = conn.prepareStatement(sql);
		
		ResultSet rs = stmt.executeQuery();
		// 디버깅 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println("총 행의 개수 stmt : "+stmt);
		
		// totalCount 저장
		if(rs.next()) {
			totalCount = rs.getInt("count(*)");
		}
		System.out.println("totalCounnt(총 행의 개수) : "+totalCount);
				
		// 마지막 페이지
		// lastPage를 전체 행의 수와 한 페이지에 보여질 행의 수(rowPerPage)를 이용하여 구한다
		lastPage = totalCount / ROW_PER_PAGE;
		if(totalCount % ROW_PER_PAGE != 0) {
			lastPage+=1;
			}
		System.out.println("lastPage(마지막 페이지 번호) : "+lastPage);
				
		rs.close();
		stmt.close();
		conn.close();
				
		return lastPage;
	}
	
	// [관리자] 특정 상품평을 취소하는 메서드
	// 받아온 orderNo을 가지고 있는 orderComment의 테이블의 행 삭제
	// <-- 상품평 입력 시 중복 입력을 못하기 때문에 가능
	public boolean deleteOrderCommentByAdmin(int orderNo) throws ClassNotFoundException, SQLException {
		boolean result = false;
		
		// 매개변수 값을 디버깅
		System.out.println(orderNo + "<--- orderCommentDao.deleteOrderCommentByAdmin parem : orderNo");
		
		// DB 실행
		// dbUtil 객체 생성
		DBUtil dbUtil = new DBUtil();
		Connection conn = dbUtil.getConnection();
		String sql = "DELETE FROM order_comment WHERE order_no=?";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, orderNo);
		
		// 디버깅 코드 : 쿼리내용과 표현식의 파라미터값 확인가능
		System.out.println(stmt + "<--- stmt");
		
		// DELETE 실행
		int row = stmt.executeUpdate();
		if(row == 1) {
			result = true;
		}
		// 종료
		stmt.close();
		conn.close();
				
		// 성공 : result = true, 실패 : false
		return result;
	}
}
