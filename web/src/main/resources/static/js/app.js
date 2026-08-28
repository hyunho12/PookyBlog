const main = {
    init() {
        this.showStoredFeedback();
        this.applyCommentOwnershipUi();
        $('#btn-save').on('click', event => this.createPost(event.currentTarget));
        $('#btn-update').on('click', event => this.updatePost(event.currentTarget));
        $('#btn-delete').on('click', event => this.deletePost(event.currentTarget));
        $('#btn-signup').on('click', event => this.signUp(event.currentTarget));
        $('#btn-login').on('click', event => this.login(event.currentTarget));
        $('#btn-logout').on('click', event => this.logout(event.currentTarget));
        $('#btn-comment-save').on('click', event => this.commentSave(event.currentTarget));
        $('.btn-comment-edit').on('click', event => this.toggleCommentEdit(event.currentTarget.dataset.commentId, true));
        $('.btn-comment-cancel').on('click', event => this.toggleCommentEdit(event.currentTarget.dataset.commentId, false));
        $('.btn-comment-update').on('click', event => this.commentUpdate(event.currentTarget));
        $('.btn-comment-delete').on('click', event => this.commentDelete(event.currentTarget));
        $('#btn-like').on('click', event => this.like(event.currentTarget));
        $('#btn-unlike').on('click', event => this.unlike(event.currentTarget));
        const postId = $('#id').val();
        if (postId && $('#view-count').length) this.increaseView(postId);
    },

    applyCommentOwnershipUi() {
        const currentUser = document.body.dataset.currentUser;
        $('.comment-item').each(function () {
            const isOwner = currentUser && this.dataset.commentAuthor === currentUser;
            $(this).find('.comment-owner-actions').toggleClass('d-none', !isOwner);
        });
    },

    ajax(method, url, data) {
        return $.ajax({method, url, contentType: 'application/json; charset=utf-8',
            data: data === undefined ? undefined : JSON.stringify(data), xhrFields: {withCredentials: true}});
    },

    validateForm(selector) {
        const form = document.querySelector(selector);
        if (!form) return true;
        form.classList.add('was-validated');
        return form.checkValidity();
    },

    setBusy(button, busy) {
        if (!button) return;
        if (busy) {
            button.dataset.originalHtml = button.innerHTML;
            button.innerHTML = `<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>${button.dataset.loadingText || '처리 중...'}`;
            button.disabled = true;
        } else {
            button.innerHTML = button.dataset.originalHtml || button.innerHTML;
            button.disabled = false;
        }
    },

    showFeedback(message, type = 'success') {
        const region = $('#feedback');
        if (!region.length) return;
        region.html(`<div class="alert alert-${type} alert-dismissible fade show" role="alert">${this.escapeHtml(message)}<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="닫기"></button></div>`);
        window.setTimeout(() => {
            const alertElement = region.find('.alert').get(0);
            if (alertElement) bootstrap.Alert.getOrCreateInstance(alertElement).close();
        }, 5000);
    },

    storeFeedback(message, type = 'success') {
        sessionStorage.setItem('pookyBlogFeedback', JSON.stringify({message, type}));
    },

    showStoredFeedback() {
        const raw = sessionStorage.getItem('pookyBlogFeedback');
        if (!raw) return;
        sessionStorage.removeItem('pookyBlogFeedback');
        try { const feedback = JSON.parse(raw); this.showFeedback(feedback.message, feedback.type); } catch (_) { /* ignore malformed UI state */ }
    },

    errorMessage(error, fallback = '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.') {
        return error.responseJSON?.message || error.responseJSON?.error || error.responseText || fallback;
    },

    escapeHtml(value) {
        return $('<div>').text(value == null ? '' : String(value)).html();
    },

    createPost(button) {
        if (!this.validateForm('#post-form')) return;
        const data = {title: $('#title').val().trim(), content: $('#content').val().trim()};
        this.setBusy(button, true);
        this.ajax('POST', '/api/posts', data).done(() => { this.storeFeedback('게시글이 등록되었습니다.'); location.href = '/'; })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    updatePost(button) {
        if (!this.validateForm('#post-form')) return;
        const id = $('#id').val(), data = {title: $('#title').val().trim(), content: $('#content').val().trim()};
        if (!confirm('게시글을 수정하시겠습니까?')) return;
        this.setBusy(button, true);
        this.ajax('PATCH', `/api/posts/${id}`, data).done(() => { this.storeFeedback('게시글이 수정되었습니다.'); location.href = `/posts/getPost/${id}`; })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    deletePost(button) {
        const id = $('#id').val();
        if (!confirm('게시글을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;
        this.setBusy(button, true);
        this.ajax('DELETE', `/api/posts/${id}`).done(() => { this.storeFeedback('게시글이 삭제되었습니다.'); location.href = '/'; })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    signUp(button) {
        if (!this.validateForm('#signup-form')) return;
        const data = {username: $('#username').val().trim(), password: $('#password').val(), nickname: $('#nickname').val().trim(), email: $('#email').val().trim()};
        this.setBusy(button, true);
        this.ajax('POST', '/api/auth/signup', data).done(() => { this.storeFeedback('회원가입이 완료되었습니다. 로그인해 주세요.'); location.href = '/auth/login'; })
            .fail(error => this.showFeedback(this.errorMessage(error, '회원가입 정보를 확인해 주세요.'), 'danger')).always(() => this.setBusy(button, false));
    },

    login(button) {
        if (!this.validateForm('#login-form')) return;
        const data = {username: $('#username').val().trim(), password: $('#password').val()};
        $('#error-message').addClass('d-none').empty();
        this.setBusy(button, true);
        this.ajax('POST', '/api/auth/login', data).done(() => { this.storeFeedback('로그인되었습니다.'); location.href = '/'; })
            .fail(error => $('#error-message').removeClass('d-none').text(this.errorMessage(error, '아이디 또는 비밀번호를 확인해 주세요.')))
            .always(() => this.setBusy(button, false));
    },

    logout(button) {
        this.setBusy(button, true);
        this.ajax('POST', '/api/auth/logout').done(() => { this.storeFeedback('안전하게 로그아웃되었습니다.'); location.href = '/'; })
            .fail(error => this.showFeedback(this.errorMessage(error, '로그아웃하지 못했습니다.'), 'danger')).always(() => this.setBusy(button, false));
    },

    commentSave(button) {
        if (!this.validateForm('#comment-form')) return;
        const postId = $('#postsId').val(), comment = $('#comment').val().trim();
        this.setBusy(button, true);
        this.ajax('POST', `/api/posts/${postId}/comments`, {comment}).done(() => { this.storeFeedback('댓글이 등록되었습니다.'); location.reload(); })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    toggleCommentEdit(id, open) {
        $(`#comment-edit-${id}`).toggleClass('is-open', open);
        if (open) $(`#comment-content-${id}`).trigger('focus');
    },

    commentUpdate(button) {
        const id = button.dataset.commentId, content = $(`#comment-content-${id}`).val().trim();
        if (!content) { this.showFeedback('댓글 내용을 입력해 주세요.', 'warning'); return; }
        this.setBusy(button, true);
        this.ajax('PUT', `/api/comments/${id}`, {content}).done(() => { this.storeFeedback('댓글이 수정되었습니다.'); location.reload(); })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    commentDelete(button) {
        const id = button.dataset.commentId;
        if (!confirm('댓글을 삭제하시겠습니까?')) return;
        this.setBusy(button, true);
        this.ajax('DELETE', `/api/comments/${id}`).done(() => { this.storeFeedback('댓글이 삭제되었습니다.'); location.reload(); })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    like(button) {
        const id = $('#id').val(); this.setBusy(button, true);
        this.ajax('POST', `/api/posts/${id}/likes`).done(() => { this.showFeedback('이 게시글을 좋아합니다.'); this.refreshLikeCount(id); })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    unlike(button) {
        const id = $('#id').val(); this.setBusy(button, true);
        this.ajax('DELETE', `/api/posts/${id}/likes`).done(() => { this.showFeedback('좋아요를 취소했습니다.', 'secondary'); this.refreshLikeCount(id); })
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger')).always(() => this.setBusy(button, false));
    },

    renderCount(selector, response) {
        const count = response && typeof response === 'object' && response.count !== undefined
            ? response.count : response;
        $(selector).text(String(count));
    },

    refreshLikeCount(id) {
        return $.get(`/api/posts/${id}/likes/count`)
            .done(count => this.renderCount('#like-count', count))
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger'));
    },

    refreshViewCount(id) {
        return $.get(`/api/posts/${id}/views/count`)
            .done(count => this.renderCount('#view-count', count))
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger'));
    },

    increaseView(id) {
        this.ajax('POST', `/api/posts/${id}/views`)
            .done(() => this.refreshViewCount(id))
            .fail(error => this.showFeedback(this.errorMessage(error), 'danger'));
    }
};

$(function () { main.init(); });
