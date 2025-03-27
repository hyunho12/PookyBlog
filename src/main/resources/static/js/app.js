const main = {
    init : function (){
        const _this = this;

        // 페이지 로드시 JWT 인증확인
        if (window.location.pathname !== '/' && window.location.pathname !== '/auth/login') {
            _this.checkAuth();
        }

        //게시글 저장
        $('#btn-save').on('click', function () {
            //_this.checkAuth();
            _this.createPost();
        });

        //게시글 수정
        $('#btn-update').on('click', function () {
            _this.updatePost();
        });

        //게시글 삭제
        $('#btn-delete').on('click', function () {
            _this.delete();
        });

        //회원가입
        $('#btn-signup').on('click', function () {
            _this.signUp();
        });

        //로그인
        $('#btn-login').on('click', function () {
            _this.login();
        });

        //로그아웃
        $('#btn-logout').on('click', function () {
            _this.logout();
        });

        //댓글저장
        $('#btn-comment-save').on('click', function () {
            _this.commentSave();
        });
    },

    checkAuth: function () {
        const token = localStorage.getItem('jwtToken');
        if(!token){
            return;
        }

        $.ajax({
            type: 'GET',
            url: '/auth/check-auth',
            xhrFields: { withCredentials: true }
        }).done(function (response) {
            console.log("인증 확인 성공:", response);
        }).fail(function (error) {
            console.log("인증 실패:", error);
            alert("로그인이 필요합니다.");
            localStorage.removeItem('jwtToken')
            window.location.href = '/auth/login'; // 로그인 페이지로 이동
        });
    },

    createPost : function (){
        // if(!jwtToken){
        //     alert("로그인이 필요합니다.");
        //     window.location.href = '/auth/login';
        //     return;
        // }

        const data = {
            title: $('#title').val(),
            writer: $('#writer').val(),
            content: $('#content').val()
        };

        if(!data.title || data.title.trim() === "" || !data.content || data.content.trim() === ""){
            alert("공백 또는 입력되지 않은 부분이 있습니다.");
            return false;
        }
        else{
            $.ajax({
                type: 'POST',
                url: '/posts/create',
                dataType: 'JSON',
                contentType: 'application/json; charset=utf-8',
                xhrFields: { withCredentials: true }, // 쿠키 포함
                data: JSON.stringify(data)
            }).done(function () {
                alert('등록되었습니다.');
                window.location.href = '/';
            }).fail(function (error) {
                alert(JSON.stringify(error));
            });
        }
    },

    updatePost : function (){
        const data = {
            id: $('#id').val(),
            title: $('#title').val(),
            content: $('#content').val()
        };

        const con_check = confirm("수정하시겠습니까?");
        if(con_check === true){
            if(!data.title || data.title.trim() === "" || !data.content || data.content.trim() === ""){
                alert("공백 또는 입력하지 않은 부분이 있습니다.");
                return false;
            }
            else{
                $.ajax({
                    type: 'PATCH',
                    url: '/posts/update/' + data.id,
                    dataType: 'JSON',
                    contentType: 'application/json; charset=utf-8',
                    xhrFields: { withCredentials: true }, // 쿠키 포함
                    data: JSON.stringify(data)
                }).done(function (){
                    alert("수정되었습니다.");
                    window.location.href = '/posts/getPost/' + data.id;
                }).fail(function (error){
                    alert(JSON.stringify(error));
                });
            }
        }

    },

    delete : function () {
        const id = $('#id').val();
        const con_check = confirm("정말 삭제하시겠습니까?");

        if(con_check == true) {
            $.ajax({
                type: 'DELETE',
                url: '/posts/delete/'+id,
                dataType: 'JSON',
                contentType: 'application/json; charset=utf-8',
                xhrFields: { withCredentials: true } // 쿠키 포함
            }).done(function () {
                alert("삭제되었습니다.");
                window.location.href = '/';
            }).fail(function (error) {
                alert(JSON.stringify(error));
            });
        } else {
            return false;
        }
    },

    signUp: function () {
        const data = {
            username: $('#username').val(),
            password: $('#password').val(),
            nickname: $('#nickname').val(),
            email: $('#email').val()
        };

        // 필드 검증
        if (!data.username || data.username.trim() === "" ||
            !data.password || data.password.trim() === "" ||
            !data.nickname || data.nickname.trim() === "" ||
            !data.email || data.email.trim() === "") {
            alert("공백 또는 입력되지 않은 부분이 있습니다.");
            return false;
        }

        $.ajax({
            type: 'POST',
            url: '/auth/signup',
            dataType: 'JSON',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert("회원가입이 완료되었습니다.");
            window.location.href = '/auth/login'; // 로그인 페이지로 이동
        }).fail(function (error) {
            // 에러 메시지를 화면에 표시
            if (error.responseJSON) {
                const errors = error.responseJSON;
                if (errors.valid_username) {
                    $('#valid_username').text(errors.valid_username);
                }
                if (errors.valid_password) {
                    $('#valid_password').text(errors.valid_password);
                }
                if (errors.valid_nickname) {
                    $('#valid_nickname').text(errors.valid_nickname);
                }
                if (errors.valid_email) {
                    $('#valid_email').text(errors.valid_email);
                }
            } else {
                alert("회원가입에 실패했습니다.");
            }
        });
    },

    login: function () {
        const data = {
            username: $('#username').val(),
            password: $('#password').val()
        };

        if (!data.username || data.username.trim() === "" || !data.password || data.password.trim() === "") {
            $('#error-message').text("아이디와 비밀번호를 입력해주세요.");
            return false;
        }

        $.ajax({
            type: 'POST',
            url: '/auth/login',
            dataType: 'JSON',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data),
            xhrFields: { withCredentials: true } // 쿠키 포함
        }).done(function () {
            alert("로그인 성공!");
            window.location.href = '/';
        }).fail(function (error) {
            if (error.responseJSON && error.responseJSON.message) {
                $('#error-message').text(error.responseJSON.message);
            } else {
                $('#error-message').text("로그인 중 오류가 발생했습니다.");
            }
        });
    },

    logout: function () {
        $.ajax({
            type: 'POST',
            url: '/auth/logout',
            dataType: 'JSON',
            contentType: 'application/json; charset=utf-8',
            xhrFields: { withCredentials: true } // 쿠키 포함
        }).done(function () {
            alert("로그아웃 되었습니다.");
            window.location.href = '/';
        }).fail(function (error) {
            alert("로그아웃 실패: " + JSON.stringify(error));
        });
    },

    commentSave : function () {
        const data = {
            postsId: $('#postsId').val(),
            userId: $('#userId').val(),
            comment: $('#comment').val()
        }

        // 값이 정상적으로 가져와지는지 로그 확인
        console.log("postsId: ", data.postsId);
        console.log("userId: ", data.userId);
        console.log("comment: ", data.comment);

        // 공백 및 빈 문자열 체크
        if (!data.postsId || !data.userId || !data.comment || data.comment.trim() === "") {
            alert("공백 또는 입력하지 않은 부분이 있습니다.");
            return false;
        } else {
            $.ajax({
                type: 'POST',
                url: '/comments/create',
                dataType: 'JSON',
                contentType: 'application/json; charset=utf-8',
                data: JSON.stringify(data)
            }).done(function () {
                alert('댓글이 등록되었습니다.');
                window.location.reload();
            }).fail(function (error) {
                alert(JSON.stringify(error));
            });
        }
    }
};

main.init();