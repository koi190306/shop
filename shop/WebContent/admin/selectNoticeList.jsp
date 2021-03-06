<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="vo.*" %>
<%@ page import="dao.*" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>공지사항 목록</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<%
	System.out.println("----------selectNoticeList.jsp----------");
	request.setCharacterEncoding("utf-8");
	
	// noticeDao 객체 생성
	NoticeDao noticeDao = new NoticeDao();
	
	// adminPage의 방어코드
	// session에 저장된 loginMember를 받아옴
	Member loginMember = (Member)session.getAttribute("loginMember");
	// loginMember가 null이거나 memberLevel이 1이하 일 때 이 페이지를 들어올 수 없음
	if(loginMember == null || loginMember.getMemberLevel() < 1){
		response.sendRedirect(request.getContextPath()+"/index.jsp");
		return;
	}
	
	// 검색어
	String searchNoticeTitle = "";
	// searchNoticeTitle이 null이 아니라면 값을 받아서 검색어(searchNoticeTitle)로 사용
	if(request.getParameter("searchNoticeTitle") != null) { 
		searchNoticeTitle = request.getParameter("searchNoticeTitle");
	}
	// 디버깅
	System.out.println("searchNoticeTitle(검색어) : "+searchNoticeTitle);
	
	// 페이지번호 = 전달 받은 값이 없으면 currentPage를 1로 디폴트
	int currentPage = 1;
	// current가 null이 아니라면 값을 int 타입으로로 바꾸어서 페이지 번호로 사용
	if(request.getParameter("currentPage") != null) { 
		currentPage = Integer.parseInt(request.getParameter("currentPage"));
	}
	// 디버깅
	System.out.println("currentPage(현재 페이지 번호) : "+currentPage);
	
	// limit 값 설정 beginRow부터 rowPerPage만큼 보여주세요
	// ROW_PER_PAGE 변수를 상수로 설정하여서 10으로 초기화하면 끝까지 10이다.
	final int ROW_PER_PAGE = 10;
	int beginRow = (currentPage-1) * ROW_PER_PAGE;
	
	// 검색어가 없을때는 전체 공지사항을 SELECT하고, 페이징하는 noticeDao의 selectNoticeList메서드 호출
	// 검색어가 있을때는 Like를 사용한 sql문을 실행하고 리스트를 리턴한 selectNoticeListBySearch메서드 호출
	// noticeList라는 리스트를 사용하기 위해 생성
	ArrayList<Notice> noticeList = new ArrayList<Notice>();
	if(searchNoticeTitle.equals("") == true) { // 검색어가 없을 때
		noticeList = noticeDao.selectNoticeList(beginRow, ROW_PER_PAGE);
	}else { // 검색어가 있을때
		noticeList = noticeDao.selectNoticeListBySearch(beginRow, ROW_PER_PAGE, searchNoticeTitle);
	}
%>
<body>
	<div class="container-fluid">
		<div class="row">
			<!-- start: adminMenu include -->
			<div class="col-sm-2 bg-light">
				<jsp:include page="/partial/adminMenu.jsp"></jsp:include>
			</div>
			<!-- end : adminMenu include -->
			
			<div class="col-sm-10 mt-5">
			
				<h3 class="font-weight-bold">공지게시판 관리</h3>
				
				<!-- 공지사항 추가하기 -->
				<div class="float-right">
					<a class="btn btn-dark" href="<%=request.getContextPath()%>/admin/insertNoticeForm.jsp">공지사항 추가하기</a>
				</div>
				
				<table class="table table-bordered table-sm text-center mt-5">
					<thead>
						<tr>
							<th>NO</th>
							<th>TITLE</th>
							<th>WRITER</th>
							<th>UPDATE DATE</th>
							<th>CREATE DATE</th>
							<th>UPDATE</th>
							<th>DELETE</th>
						</tr>
					</thead>
					<tbody>
						<%
							// noticeList를 출력하는 for문
							for(Notice n : noticeList) {
						%>
								<tr>
									<td><%=n.getNoticeNo()%></td>
									<td>
										<a class="text-body" href="<%=request.getContextPath()%>/selectNoticeOne.jsp?noticeNo=<%=n.getNoticeNo()%>"><%=n.getNoticeTitle()%></a>
									</td>
									<td><%=n.getMemberNo()%>
									<td><%=n.getUpdateDate()%></td>
									<td><%=n.getCreateDate()%></td>
									<td>
										<!-- 로그인된 관리자의 비밀번호를 확인 후 공지 게시글을 수정 -->
										<a class="btn btn-light btn-sm" href="<%=request.getContextPath()%>/admin/updateNoticeForm.jsp?noticeNo=<%=n.getNoticeNo()%>">수정</a>
									</td>
									<td>
										<!-- 로그인된 관리자의 비밀번호를 확인 후 공지 게시글을 삭제 -->
										<a class="btn btn-light btn-sm" href="<%=request.getContextPath()%>/admin/deleteNotice.jsp?noticeNo=<%=n.getNoticeNo()%>">삭제</a>
									</td>
								</tr>
						<%
							}
						%>
					</tbody>
				</table>
					<%
					// 마지막 페이지(lastPage)를 구하는 noticeDao의 메서드 호출
					// int 타입의 lastPage에 저장
					// 검색어가 없을때는 전체 행을 COUNT 하는 selectNoticeListLastPage메서드 호출
					// 검색어가 있을때는 LIKE 연산자를 사용한 sql문을 실행하고 lastPage를 리턴하는 selectNoticeListSearchLastPage메서드 호출
					int lastPage;
					if(searchNoticeTitle.equals("") == true) { // 검색어가 없을때
						lastPage = noticeDao.selectNoticeListLastPage(ROW_PER_PAGE);
					} else { // 검색어가 있을때
						lastPage = noticeDao.selectNoticeListSearchLastPage(ROW_PER_PAGE,searchNoticeTitle);
					}
					
					// 화면에 보여질 페이지 번호의 갯수
					int displayPage = 10;
					
					// 화면에 보여질 시작 페이지 번호
					// ((현재페이지번호 - 1) / 화면에 보여질 페이지 번호) * 화면에 보여질 페이지 번호 + 1
					// (currentPage - 1)을 하는 이유는 현재페이지가 10일시에도 startPage가 1이기 위해서
					int startPage = ((currentPage - 1) / displayPage) * displayPage + 1;
			
					// 화면에 보여질 마지막 페이지 번호
					// 만약에 마지막 페이지 번호(lastPage)가 화면에 보여질 페이지 번호(displayPage)보다 작다면 화면에 보여질 마지막 페이지번호(endPage)를 조정한다
					// 화면에 보여질 시작 페이지 번호 + 화면에 보여질 페이지 번호 - 1
					// -1을 하는 이유는 페이지 번호의 갯수가 10개이기 때문에 statPage에서 더한 1을 빼준다
					int endPage = 0;
					if(lastPage<displayPage){
						endPage = lastPage;
					} else if (lastPage>=displayPage){
						endPage = startPage + displayPage - 1;
					}
					
					// 디버깅
					System.out.println("startPage(화면에 보여질 시작 페이지 번호) : "+startPage+", endPage(화면에 보여질 마지막 페이지 번호) : "+endPage);
					
					// 처음으로 버튼
					// 제일 첫번째 페이지로 이동할때 = 1 page로 이동
					if(currentPage != 1){
					%>
						<a href="<%=request.getContextPath()%>/admin/selectNoticeList.jsp?currentPage=<%=1%>&searchNoticeTitle=<%=searchNoticeTitle%>" class="btn btn-outline-secondary center-block">◀처음</a>
					<%
					}
			
					// 이전 버튼
					// 화면에 보여질 시작 페이지 번호가 화면에 보여질 페이지 번호의 갯수보다 크다면 이전 버튼을 생성
					if(startPage > displayPage){
					%>
						<a href="<%=request.getContextPath()%>/admin/selectNoticeList.jsp?currentPage=<%=startPage-displayPage%>&searchNoticeTitle=<%=searchNoticeTitle%>" class="btn btn-outline-secondary">&lt;이전</a>
					<%
					}
					
					// 페이징버튼
					// 화면에 보여질 시작 페이지 번호를 화면에 보여질 마지막 페이지 번호까지 반복하면서 페이지 번호 생성
					// 만약에 화면에 보여질 마지막 페이지 번호가 마지막 페이지보다 크다면 for문을 break로 종료시킴
					for(int i=startPage; i<=endPage; i++){
						if(currentPage == i){
					%>
							<a href="<%=request.getContextPath()%>/admin/selectNoticeList.jsp?currentPage=<%=i%>&searchNoticeTitle=<%=searchNoticeTitle%>" class="btn btn-secondary"><%=i%></a>
					<%
						} else if(endPage<lastPage || endPage == lastPage){
					%>
							<a href="<%=request.getContextPath()%>/admin/selectNoticeList.jsp?currentPage=<%=i%>&searchNoticeTitle=<%=searchNoticeTitle%>" class="btn btn-outline-secondary"><%=i%></a>
					<%	
						} else if(endPage>lastPage){
							break;
						}
					}
			
					// 다음 버튼
					// 화면에 보여질 마지막 페이지 번호가 마지막페이지보다 작다다면 이전 버튼을 생성
					if(endPage < lastPage){
					%>
						<a href="<%=request.getContextPath()%>/admin/selectNoticeList.jsp?currentPage=<%=startPage+displayPage%>&searchNoticeTitle=<%=searchNoticeTitle%>" class="btn btn-outline-secondary">다음></a>
					<%
					}
					
					// 끝으로 버튼
					// 가장 마지막 페이지로 바로 이동하는 버튼
					if(currentPage != lastPage && endPage != 0){
					%>
						<a href="<%=request.getContextPath()%>/admin/selectNoticeList.jsp?currentPage=<%=lastPage%>&searchNoticeTitle=<%=searchNoticeTitle%>" class="btn btn-outline-secondary">끝▶</a>
					<%
					}
					%>
						
				<!-- notice_title로 검색 -->
				<div class="float-right">
					<form action="<%=request.getContextPath()%>/admin/selectNoticeList.jsp" method="get">
						Search with TITLE <input type="text" name="searchNoticeTitle" placeholder="검색어를 입력해주세요">
						<button class="btn btn-dark btn-sm" type="submit">검색</button>
					</form>
				</div>
				
			</div>
			
		</div>
	</div>
</body>
</html>