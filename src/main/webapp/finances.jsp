<%@ taglib tagdir="/WEB-INF/tags" prefix="tags" %>

<tags:head/>

<body class="cc-page-finances">
	<tags:header/>
	<tags:topnavigation/>
	<div class="cc-container-main" role="main">
		<!-- Page specific HTML -->
		<div class="cc-page-finances-column1 cc-left">
			<div class="cc-container-widget cc-widget-financialstatus">
				<div class="cc-widget-title">
					<h2>My Financial Status</h2>
				</div>
				<div class="cc-financial-background-alert cc-widget-body">
					<ul>
						<li><a href="#" class="cc-alert" data-notreal="standard"><div class="cc-icon-big cc-icon-big-alert cc-right"></div><h3>Registration block</h3>Unpaid CARS bill</a></li>
					</ul>
					<h3 class="cc-left">Financial Notifications:</h3>
					<ul>
						<li><a href="#" class="cc-alert" data-notreal="standard">FAFSA Application Deadline</a><span class="cc-right cc-alert">Today</a></li>
					</ul>
				</div>
				<h3>MyFinAid Notifications:</h3>
				<ul>
					<li><a href="#" data-notreal="finaidcheck">You have 1 missing document</a></li>
					<li><a href="#" data-notreal="finaidcheck">You have 1 new message</a></li>
				</ul>
				<h3>Expected Graduation:</h3> <a href="#" data-notreal="finaidchange">Spring 2013</a>
				<ul class="cc-page-finances-logins">
					<li><a href="https://myfinaid.berkeley.edu" class="cc-icon-arrow-right">Log in to MyFinAid</a></li>
					<li><a href="https://bearfacts.berkeley.edu" class="cc-icon-arrow-right">Log in to BearFacts</a></li>
				</ul>
			</div>
			<div class="cc-container-widget">
				<div class="cc-widget-title">
					<h2>Financial Resources</h2>
				</div>
				<div>
					<ul class="cc-widget-list-triangle">
						<li><a href="http://students.berkeley.edu/finaid">UC Berkeley Financial Aid &amp; Scholarships</a></li>
						<li><a href="http://cal1card.berkeley.edu">Cal One Card</a></li>
						<li><a href="http://studentbilling.berkeley.edu">Student Billing Services</a></li>
						<li><a href="http://www.fafsa.ed.gov">FAFSA</a></li>
						<li><a href="https://studentloans.gov">StudentLoans.gov</a></li>
						<li><a href="http://scholarships.berkeley.edu">UCB Scholarship Connection</a></li>
						<li><a href="http://students.berkeley.edu/finaid/undergraduates/types_loans.htm">UCB Undergraduate Loan Programs</a></li>
						<li><a href="http://registrar.berkeley.edu/feesched.html">2012-2013 Reg Fees</a></li>
					</ul>
				</div>
			</div>
		</div>
		<div class="cc-page-finances-column2 cc-left">
			<div class="cc-container-widget cc-widget-quickstatement">
				<div class="cc-widget-title">
					<h2>Quick Statement</h2>
				</div>
				<div>
					<dl class="cc-left">
						<dt>Min. Amount Due:</dt>
						<dd>&#36;200.00</dd>
						<dt>Account Balance:</dt>
						<dd>&#36;1492.75</dd>
					</dl>
					<p class="cc-left cc-widget-quickstatement-paybill">
						<a href="#" data-notreal="standard" class="cc-icon-alert">
							<span class="cc-icon-arrow-right-brown"></span>
							Pay My Bill<br />
							<em>Due 12/1/12</em>
						</a>
					</p>
				</div>
			</div>
			<div class="cc-container-widget cc-widget-awardsandbill">
				<div class="cc-widget-title">
					<h2>My Awards &amp; Bill</h2>
				</div>
				<div>
					<ul class="clearfix">
						<li><a class="cc-selected" href="#">Fall 2012</a></li>
						<li><a href="#" data-notreal="standard">Spring 2013</a></li>
						<li><a href="#" data-notreal="standard">Total</a></li>
					</ul>
					<dl class="cc-left">
						<dt>Awards Offered:</dt>
						<dd>&#36;7000.00</dd>
						<dt>Awards Accepted:</dt>
						<dd>&#36;6000.00</dd>
						<dt>Awards Disbursed:</dt>
						<dd>&#36;6000.00</dd>
						<dt class="cc-widget-awardsandbill-last">UC Berkeley Bill:</dt>
						<dd class="cc-widget-awardsandbill-last">&#36;7492.75</dd>
					</dl>
					<a href="#" data-notreal="standard">
						<img class="cc-right" src="/img/myb/graph_awards_and_bill.png" />
					</a>
				</div>
			</div>
			<div class="cc-container-widget cc-widget-awardsandbillhistory">
				<div class="cc-widget-title">
					<h2>Award &amp; Bill History</h2>
				</div>
				<div>
					<a href="#" data-notreal="standard">
						<img class="cc-right" src="/img/myb/graph_awards_and_bill_history.png" />
					</a>
				</div>
			</div>
		</div>
		<div class="cc-page-finances-column3 cc-left">
			<div class="cc-page-finances-selling cc-page-finances-selling-cal1card" style="display:none">
				<h3>Cal1 Card</h3>
				<p>Your current balance:</p>
				<p class="cc-page-finances-selling-cal1card-balance">&#36;56.21</p>
				<ul>
					<li><a href="#">Make a deposit</a></li>
					<li><a href="#">Check transactions</a></li>
					<li><a href="#">Cal1Card site</a></li>
				</ul>
			</div>
			<a href="http://cal1card.berkeley.edu">
				<img src="/img/myb/cal1card.png" />
			</a>
			<a href="http://www.cashcourse.org/ucberkeley">
				<img src="/img/myb/planning_budget.png" />
			</a>
			<div class="cc-container-widget cc-widget-scholarshipideas">
				<div class="cc-widget-title">
					<h2>Scholarship Opportunities</h2>
				</div>
				<div>
					<ul>
						<li><a href="http://scholarships.berkeley.edu">10 Degrees Scholarships</a></li>
						<li><a href="http://scholarships.berkeley.edu">Academy of Motion Picture Arts &amp; Sciences Student Film Awards</a></li>
						<li><a href="http://scholarships.berkeley.edu">Asian American Journalists Association Scholarships</a></li>
						<li><a href="http://scholarships.berkeley.edu">Bibliographical Society of America Fellowship Program</a></li>
						<li><a href="http://scholarships.berkeley.edu">Boren Scholarships for International Study</a></li>
						<li><a href="http://scholarships.berkeley.edu">Bowman Travel Grants</a></li>
					</ul>
				</div>
				<p class="cc-page-campus-more cc-right"><a href="#">More scholarships &#9658;</a></p>
			</div>
		</div>
		<!-- END Page specific HTML -->
	</div>
	<tags:footer/>
</body>
</html>
