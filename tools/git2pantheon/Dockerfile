FROM fedora:30 AS builder
#Force the use of the mirror: http://download-ib01.fedoraproject.org/pub
RUN sed -e '/metalink/s/^/#/g' -i /etc/yum.repos.d/fedora.repo && sed -i '/baseurl/s/^#//g' /etc/yum.repos.d/fedora.repo && sed -i 's/download/download-ib01/g' /etc/yum.repos.d/fedora.repo && sed -e '/metalink/s/^/#/g' -i /etc/yum.repos.d/fedora-updates.repo && sed -i '/baseurl/s/^#//g' /etc/yum.repos.d/fedora-updates.repo && sed -i 's/download/download-ib01/g' /etc/yum.repos.d/fedora-updates.repo && sed -e '/metalink/s/^/#/g' -i /etc/yum.repos.d/fedora-updates-modular.repo && sed -i '/baseurl/s/^#//g' /etc/yum.repos.d/fedora-updates-modular.repo && sed -i 's/download/download-ib01/g' /etc/yum.repos.d/fedora-updates-modular.repo && sed -e '/metalink/s/^/#/g' -i /etc/yum.repos.d/fedora-modular.repo && sed -i '/baseurl/s/^#//g' /etc/yum.repos.d/fedora-modular.repo && sed -i 's/download/download-ib01/g' /etc/yum.repos.d/fedora-modular.repo
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
FROM registry.access.redhat.com/ubi8/python-36
RUN pip install requests pyyaml
COPY --from=builder /go/app/main .
CMD ["./main"]
EXPOSE 9666/tcp