FROM fedora:30 AS builder
RUN dnf -y update && dnf -y install golang && dnf clean all
RUN mkdir -p /go && chmod -R 777 /go
ENV GOPATH=/go
WORKDIR /go
RUN mkdir /go/app 
ADD . /go/app/
WORKDIR /go/app 
RUN go get gopkg.in/src-d/go-git.v4
RUN go build -o main .

#Use RHEL 8 Universal Base Image with python-3.6 support as runtime.
FROM ubi8/python-36
RUN pip install requests pyyaml
COPY --from=builder /go/app/main .
CMD ["./main"]
EXPOSE 9666/tcp