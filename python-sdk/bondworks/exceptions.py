class BondWorksError(Exception):
    """Base SDK error."""


class BondWorksApiError(BondWorksError):
    def __init__(self, status_code: int, code: str | None, message: str):
        super().__init__(f"{status_code} {code or 'API_ERROR'}: {message}")
        self.status_code = status_code
        self.code = code
        self.message = message
